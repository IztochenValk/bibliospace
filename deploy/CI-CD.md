# Pipeline CI/CD — BiblioSpace

> Documentation complète du pipeline d'intégration et de déploiement
> continu basé sur GitHub Actions.
> Lecture utile pour : comprendre le flux, ajouter un secret, débugger un
> déploiement qui plante, ou onboarder un nouvel intervenant sur le projet.

---

## Vue d'ensemble

Le pipeline est défini dans **un seul fichier** : `.github/workflows/build-and-push.yml`.

Il se déclenche automatiquement à chaque `push` sur la branche `main` qui
touche le code applicatif ou la configuration de déploiement, et peut
également être lancé manuellement via le bouton **Run workflow** de l'onglet
Actions sur GitHub (utile pour redéployer sans changement de code, par
exemple après une rotation de secret).

```
push main → 4 jobs chaînés :

prepare ──┬─→ build-backend  ──┐
          ├─→ build-frontend ──┴─→ deploy
```

Durée totale typique : **2 à 4 minutes** quand le cache GitHub Actions est chaud,
jusqu'à 6 minutes pour un cold start (premier build après un changement de
dépendance Maven ou npm).

---

## Déclencheurs

```yaml
on:
  push:
    branches: [main]
    paths:
      - 'library-backend/**'
      - 'library-frontend/**'
      - 'deploy/**'
      - '.github/workflows/**'
  workflow_dispatch:
```

Concrètement :
- Un commit qui ne touche QUE le README, la doc ou le mémoire **ne déclenche
  pas** le pipeline (économie de minutes CI/CD).
- Un commit qui touche `library-backend/`, `library-frontend/`, `deploy/`
  ou le workflow lui-même **déclenche** un déploiement complet.
- Le bouton manuel **Run workflow** permet de redéployer la version
  actuelle de `main` sans changement de code.

---

## Job 1 — `prepare` (≈ 2 secondes)

```yaml
prepare:
  name: Préparation du namespace d'image
  runs-on: ubuntu-latest
  outputs:
    namespace: ${{ steps.ns.outputs.namespace }}
  steps:
    - id: ns
      run: echo "namespace=${GITHUB_REPOSITORY_OWNER,,}" >> "$GITHUB_OUTPUT"
```

**Rôle** : convertir le login GitHub de l'owner du repo en minuscules,
parce que la spec OCI (Open Container Initiative) impose des noms
d'images Docker en minuscules. `github.repository_owner` retourne le
nom avec sa casse d'origine (par exemple `IztochenValk`), ce qui produit
un tag invalide rejeté par buildx au push GHCR.

L'output `namespace` est consommé par les jobs `build-backend`,
`build-frontend` et `deploy`.

---

## Jobs 2 et 3 — `build-backend` / `build-frontend` (≈ 30s à 3 min)

Structure identique pour les deux, seul le contexte de build diffère :

```yaml
build-backend:
  needs: prepare
  steps:
    - uses: actions/checkout@v4
    - uses: docker/setup-buildx-action@v3
    - uses: docker/login-action@v3 (GHCR avec GITHUB_TOKEN)
    - uses: docker/build-push-action@v6
      with:
        context: ./library-backend
        push: true
        tags: |
          ghcr.io/.../bibliospace-back:latest
          ghcr.io/.../bibliospace-back:${{ github.sha }}
        cache-from: type=gha,scope=back
        cache-to:   type=gha,scope=back,mode=max
```

Points-clés :

1. **`docker/setup-buildx-action`** active BuildKit (le moteur de build
   moderne) pour bénéficier du cache distribué et du build multi-stage
   efficace.

2. **Authentification GHCR via `GITHUB_TOKEN`** : le runner s'authentifie
   sur le registry GHCR avec le token éphémère du workflow, qui a la
   permission `packages: write` déclarée plus haut dans le job.

3. **Cache GitHub Actions** (`type=gha`) : avec un scope dédié par image
   (`back` / `front`), les couches Docker non modifiées sont réutilisées
   entre exécutions. Un build sans modification de dépendance passe ainsi
   de ~3 minutes à ~30 secondes.

4. **Double tag** : chaque image est poussée avec deux tags simultanés :
   - `latest` → utilisé par le compose de prod pour le déploiement
   - `${{ github.sha }}` → traçabilité, et utile pour un rollback rapide
     (`docker pull ghcr.io/.../bibliospace-back:<sha>`)

5. **Labels OCI** : chaque image est annotée avec `org.opencontainers.image.*`
   (`source`, `revision`, `title`, `description`) qui apparaissent dans
   l'interface GHCR.

---

## Job 4 — `deploy` (≈ 30s à 1 min)

C'est le job qui contient la logique métier de déploiement. Il s'exécute
en SSH sur la VM Alibaba Cloud cible et enchaîne 8 étapes.

### Étape 1 — `Configuration de la clé SSH`

```bash
mkdir -p ~/.ssh
echo "${{ secrets.SSH_PRIVATE_KEY }}" > ~/.ssh/deploy_key
chmod 600 ~/.ssh/deploy_key
ssh-keyscan -H ${{ secrets.SSH_HOST }} >> ~/.ssh/known_hosts
```

La clé privée stockée dans le secret `SSH_PRIVATE_KEY` est écrite sur le
filesystem du runner. Le runner GitHub Actions est éphémère et isolé,
donc ces fichiers disparaissent à la fin de la session.

### Étape 2 — `Synchronisation du dossier deploy/ vers la VM`

Un `rsync` incrémental copie le contenu du dossier `deploy/` du repo vers
`~/bibliospace/` sur la VM. Le fichier `.env` et `RUNBOOK.md` sont
explicitement exclus pour ne pas écraser des artefacts générés ou la
documentation opérationnelle hors-repo.

### Étape 3 — `Génération du .env sur la VM depuis les secrets`

Le runner construit en local un fichier `.env` à partir des secrets
GitHub Actions, le transfère via `scp`, puis le passe en mode `600` sur
la VM. Les secrets sont injectés via le bloc `env:` du step, ce qui les
masque automatiquement dans les logs GitHub Actions.

Contenu typique du `.env` généré :
```env
GHCR_NAMESPACE=ghcr.io/<owner-lowercase>
IMAGE_TAG=latest
MYSQL_ROOT_PASSWORD=<random 32 chars>
MYSQL_DATABASE=bibliospace
MYSQL_USER=bibliospace
MYSQL_PASSWORD=<random 32 chars>
APP_JWT_ISSUER=bibliospace.florianchague.dev
APP_JWT_EXPIRATION_MINUTES=60
APP_DEMO_PASSWORD_ADMIN=...
APP_DEMO_PASSWORD_BIBLIOTHECAIRE=...
APP_DEMO_PASSWORD_ADHERENT=...
```

### Étape 4 — `Injection des clés RSA JWT`

Les clés publique et privée (secrets `APP_JWT_PUBLIC_KEY` et
`APP_JWT_PRIVATE_KEY`) sont écrites dans `~/bibliospace/keys/` sur la VM.
Elles sont passées via **stdin** (`<<<` heredoc) plutôt qu'en argument
de commande, ce qui évite qu'elles n'apparaissent dans la liste des
processus (`ps -ef`) ou dans les logs SSH.

Le bind mount `./keys:/app/keys:ro` du compose expose ensuite ces clés
au conteneur backend en lecture seule, et le composant `JwtCryptoConfig`
de Spring les charge via `security.jwt.public-key` / `private-key` qui
pointent en `file:/app/keys/...` en prod.

### Étape 5 — `Exécution de setup-vm.sh`

Script idempotent qui :
- vérifie que Docker et nginx sont présents sur la VM
- copie la config nginx du repo vers `/etc/nginx/sites-available/` si
  elle a changé (`cmp -s`)
- recharge nginx via `sudo systemctl reload nginx` UNIQUEMENT en cas de
  modification
- avertit si le certificat Let's Encrypt n'est pas encore généré
  (premier déploiement uniquement)

### Étape 6 — `Authentification GHCR sur la VM`

Le `GITHUB_TOKEN` du runner n'est pas disponible côté VM (token éphémère
scopé runner). On utilise donc un **Personal Access Token classique**
avec uniquement le scope `read:packages`, stocké dans le secret
`GHCR_READ_TOKEN`, pour le `docker login ghcr.io` côté VM.

```bash
echo "$GHCR_READ_TOKEN" | ssh ... \
  "docker login ghcr.io -u '...' --password-stdin"
```

Le token est piped via stdin pour la même raison de sécurité que les
clés JWT. Le login persiste ensuite dans `~/.docker/config.json` sur
la VM, donc les redémarrages manuels (`docker compose restart`) ou
les opérations DBA fonctionnent aussi sans réauthentification.

### Étape 7 — `Pull des dernières images et redémarrage de la stack`

```bash
cd ~/bibliospace
docker compose -f docker-compose.prod.yaml --env-file .env pull
docker compose -f docker-compose.prod.yaml --env-file .env up -d
```

La directive `depends_on: condition: service_healthy` du compose empêche
le démarrage du backend tant que MySQL n'a pas répondu à son healthcheck.

### Étape 8 — `Smoke tests de production`

Polling actif jusqu'à 120 secondes qui attend que la route
`/v3/api-docs` (la spec OpenAPI publique) réponde en 200. Cette
amélioration a été introduite après plusieurs faux positifs où Spring
Boot n'avait pas fini de booter (le seeder métier prend 60 à 90 secondes
lorsque le volume DB est neuf).

```bash
for i in $(seq 1 24); do
  if curl --fail --silent --max-time 5 -o /dev/null \
       https://bibliospace.florianchague.dev/v3/api-docs; then
    echo "Back prêt après $((i*5))s"
    break
  fi
  sleep 5
done

# Une fois prêt, tests finaux :
curl ... https://bibliospace.florianchague.dev/        → 200
curl ... https://bibliospace.florianchague.dev/v3/api-docs → 200
```

Si l'un des smoke tests échoue, le job retourne un code de sortie 22 et
le déploiement est marqué FAILED, ce qui apparaît en rouge dans
l'historique du pipeline.

---

## Inventaire des secrets requis (11 au total)

Tous configurés dans **Settings → Secrets and variables → Actions** sur le
repository GitHub.

| Secret | Catégorie | Usage |
|---|---|---|
| `SSH_PRIVATE_KEY` | Accès VM | Clé privée pour rsync/ssh/scp |
| `SSH_HOST` | Accès VM | IP publique de la VM Alibaba |
| `SSH_USER` | Accès VM | Compte applicatif dédié (`deploy-bibliospace`) |
| `MYSQL_ROOT_PASSWORD` | Base de données | Compte root MySQL (32 chars random) |
| `MYSQL_PASSWORD` | Base de données | Compte applicatif MySQL (32 chars random) |
| `DEMO_PASSWORD_ADMIN` | Seed users | Override du mot de passe admin démo |
| `DEMO_PASSWORD_BIBLIOTHECAIRE` | Seed users | Idem bibliothécaire |
| `DEMO_PASSWORD_ADHERENT` | Seed users | Idem adhérent |
| `GHCR_READ_TOKEN` | Registry | PAT classic scope `read:packages` |
| `APP_JWT_PRIVATE_KEY` | JWT signing | Clé RSA privée (PEM multi-ligne) |
| `APP_JWT_PUBLIC_KEY` | JWT signing | Clé RSA publique (PEM multi-ligne) |

---

## Opérations courantes

### Comment ajouter ou modifier un secret

1. Aller sur `Settings → Secrets and variables → Actions`
2. Pour un nouveau : **New repository secret**
3. Pour un existant : cliquer dessus → **Update** (la valeur courante
   n'est jamais affichée par GitHub, c'est normal et voulu)
4. Le secret est immédiatement disponible pour le prochain workflow run

### Comment déclencher un déploiement sans changement de code

Onglet **Actions** → workflow `CI/CD - Build, Push & Déploiement` →
bouton **Run workflow** en haut à droite → choisir la branche `main` →
**Run workflow**.

### Comment forcer un rebuild complet (sans cache)

Modifier transitoirement le `Dockerfile` (par exemple changer un
commentaire), commit, push. BuildKit invalide alors les couches en
aval et reconstruit depuis ce point. Le commit cosmétique peut ensuite
être squashé ou réécrit.

### Comment rollback en cas de déploiement cassé

Sur la VM en SSH :
```bash
cd ~/bibliospace
# Voir les tags disponibles dans GHCR pour identifier le commit avant
# le push problématique
docker images ghcr.io/<owner>/bibliospace-back

# Modifier .env pour pinner un SHA spécifique
sed -i 's/IMAGE_TAG=latest/IMAGE_TAG=<sha-precedent>/' .env

# Re-pull et redémarrer
docker compose -f docker-compose.prod.yaml --env-file .env pull
docker compose -f docker-compose.prod.yaml --env-file .env up -d
```

Et côté repo, ouvrir une PR de revert sur le commit fautif pour que la
prochaine push remette les choses dans un état cohérent.

### Comment diagnostiquer un déploiement qui plante

1. **Ouvrir l'onglet Actions sur GitHub** → cliquer sur le run en échec
2. **Identifier le job qui a planté** (vu son icône rouge ❌)
3. **Lire les logs du job** — chaque step expose ses sorties stdout/stderr
4. **Cas typiques** :
   - `build-*` rouge → souvent une erreur de compilation côté Maven ou
     Vite/Nuxt. Reproductible en local avec `docker build`.
   - `deploy` rouge à l'étape `Configuration de la clé SSH` →
     `SSH_PRIVATE_KEY` mal formatée ou expirée.
   - `deploy` rouge à l'étape `Pull` → problème d'authentification GHCR.
     Vérifier que `GHCR_READ_TOKEN` n'est pas expiré (les PAT classic
     expirent à 90 jours par défaut).
   - `Smoke tests` rouge → l'app a redémarré mais quelque chose dans le
     boot Spring Boot crash. SSH sur la VM puis
     `docker logs bibliospace-back` pour la stack trace.

---

## Sécurité du pipeline

- **0 secret en dur** dans le repo (vérifiable avec `git log -p` ou
  outils type gitleaks)
- **0 secret dans les images Docker** (les clés JWT sont injectées au
  runtime via bind mount, pas embedées dans le JAR)
- **0 secret dans les logs** (chaque secret passe par le bloc `env:`
  du step, ce qui déclenche le masquage automatique de GitHub Actions)
- **Le token GHCR de la VM** est en scope lecture seule, ne peut pas
  push d'images
- **Le user SSH** sur la VM (`deploy-bibliospace`) est un compte
  applicatif distinct du compte admin de la VM (séparation des
  privilèges)

---

## Évolutions possibles (non implémentées)

- **Tests automatiques en CI** (avant push d'image) : `mvn test` pour le
  back et `npm run test` + `cypress run` pour le front, en job parallèle
  à `prepare`.
- **Scan de vulnérabilités d'images** (Trivy ou Snyk) avant push GHCR.
- **Notifications Slack/Discord** en fin de déploiement.
- **Déploiement en blue/green** plutôt qu'in-place pour zéro downtime.
- **Rollback automatique** si les smoke tests échouent (`docker compose
  up -d` avec le tag précédent au lieu de marquer simplement FAILED).
