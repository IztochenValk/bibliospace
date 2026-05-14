# RUNBOOK — Déploiement BiblioSpace sur VM Alibaba

**Cible** : `bibliospace.florianchague.dev` sur VM Ubuntu 24.04 (`8.209.91.124`)
**Stack** : 3 conteneurs Docker (front Nuxt SSR + back Spring Boot + MySQL 8.4) derrière nginx + Let's Encrypt
**Durée estimée** : 1h30 à 2h pour un premier déploiement

---

## Phase 0 — Prérequis (à valider AVANT)

- [x] Clé SSH dédiée `bibliospace_deploy` ajoutée à `~/.ssh/authorized_keys` sur la VM
- [x] DNS A record `bibliospace.florianchague.dev` → `8.209.91.124` propagé (vérifier avec `nslookup`)
- [x] VM à jour, redémarrée si nécessaire
- [ ] Compte GitHub avec accès au registry GHCR (ton namespace existant : `iztochenvalk`)
- [ ] Token GHCR (PAT classic avec scope `write:packages`) disponible côté local
- [ ] Renouvellement VM Alibaba effectué (ne pas la laisser expirer le 27 mai)

---

## Phase 1 — Build et push des images Docker (sur ton poste Windows)

### 1.1 Build de l'image back

```powershell
cd C:\Users\flori\Desktop\library-project\library-backend
docker build -t ghcr.io/iztochenvalk/bibliospace-back:latest .
```

**Vérification** :
```powershell
docker images | Select-String bibliospace-back
```
L'image doit faire ~250-350 MB.

### 1.2 Build de l'image front

```powershell
cd C:\Users\flori\Desktop\library-project\nuxt-daisy-base
docker build -t ghcr.io/iztochenvalk/bibliospace-front:latest .
```

**Vérification** :
```powershell
docker images | Select-String bibliospace-front
```
L'image doit faire ~150-200 MB.

### 1.3 Auth GHCR

Créer un Personal Access Token (Classic) sur https://github.com/settings/tokens avec scope `write:packages` (et `read:packages`).

```powershell
$env:GHCR_PAT = "ghp_xxxxxxxxxxxxxxxxxxxx"
echo $env:GHCR_PAT | docker login ghcr.io -u iztochenvalk --password-stdin
```

### 1.4 Push

```powershell
docker push ghcr.io/iztochenvalk/bibliospace-back:latest
docker push ghcr.io/iztochenvalk/bibliospace-front:latest
```

### 1.5 Rendre les images publiques (recommandé)

Sur https://github.com/users/iztochenvalk/packages, pour chaque package :
- Settings → Change visibility → Public

Ainsi la VM peut pull sans avoir à s'authentifier sur GHCR. Si tu veux garder les images privées, il faudra `docker login ghcr.io` aussi sur la VM.

---

## Phase 2 — Préparation de la VM (en SSH)

```powershell
ssh -i $env:USERPROFILE\.ssh\bibliospace_deploy admin@8.209.91.124
```

### 2.1 Création de l'arborescence de déploiement

```bash
mkdir -p ~/bibliospace
cd ~/bibliospace
```

### 2.2 Copier le docker-compose.prod.yaml

Depuis ton poste Windows, dans une autre fenêtre PowerShell :

```powershell
scp -i $env:USERPROFILE\.ssh\bibliospace_deploy `
    C:\Users\flori\Desktop\library-project\deploy\docker-compose.prod.yaml `
    admin@8.209.91.124:~/bibliospace/docker-compose.prod.yaml
```

### 2.3 Créer le fichier .env sur la VM

Depuis le SSH sur la VM :

```bash
cd ~/bibliospace
cp ~/bibliospace/.env.example .env  # ou colle directement le contenu ci-dessous
nano .env
```

**Génère les mots de passe AVANT** (depuis la VM) :
```bash
openssl rand -base64 32  # → MYSQL_ROOT_PASSWORD
openssl rand -base64 32  # → MYSQL_PASSWORD
```

Et pour le mot de passe admin bibliothécaire, choisis-en un dont tu te souviendras (12+ caractères, mixte).

Contenu final attendu du `.env` :
```env
GHCR_NAMESPACE=ghcr.io/iztochenvalk
IMAGE_TAG=latest

MYSQL_ROOT_PASSWORD=<32 chars random>
MYSQL_DATABASE=bibliospace
MYSQL_USER=bibliospace
MYSQL_PASSWORD=<32 chars random>

APP_JWT_ISSUER=bibliospace.florianchague.dev
APP_JWT_EXPIRATION_MINUTES=60

APP_BOOTSTRAP_BIBLIOTHECAIRE_EMAIL=florian.chague2@gmail.com
APP_BOOTSTRAP_BIBLIOTHECAIRE_PASSWORD=<ton mot de passe admin>
APP_BOOTSTRAP_BIBLIOTHECAIRE_NOM=Chague
APP_BOOTSTRAP_BIBLIOTHECAIRE_PRENOM=Florian
```

```bash
chmod 600 .env  # IMPORTANT : protection lecture
```

Aussi : copie le .env.example sur la VM pour référence (depuis Windows) :
```powershell
scp -i $env:USERPROFILE\.ssh\bibliospace_deploy `
    C:\Users\flori\Desktop\library-project\deploy\.env.example `
    admin@8.209.91.124:~/bibliospace/.env.example
```

### 2.4 Login GHCR sur la VM (si images privées)

```bash
echo "ghp_xxxxxxxxxxxxxxxx" | docker login ghcr.io -u iztochenvalk --password-stdin
```

À skipper si tu as rendu les images publiques en 1.5.

---

## Phase 3 — Configuration nginx + HTTPS

### 3.1 Copier le site config

Depuis ton poste Windows :

```powershell
scp -i $env:USERPROFILE\.ssh\bibliospace_deploy `
    C:\Users\flori\Desktop\library-project\deploy\nginx\bibliospace.florianchague.dev.conf `
    admin@8.209.91.124:/tmp/bibliospace.florianchague.dev
```

Sur la VM :
```bash
sudo mv /tmp/bibliospace.florianchague.dev /etc/nginx/sites-available/bibliospace.florianchague.dev
sudo ln -s /etc/nginx/sites-available/bibliospace.florianchague.dev /etc/nginx/sites-enabled/
sudo nginx -t
```

Si `nginx -t` dit `syntax is ok` et `test is successful`, on continue. Sinon, lis le message d'erreur et corrige.

```bash
sudo systemctl reload nginx
```

### 3.2 Vérifier que nginx répond en HTTP (sans SSL pour l'instant)

Depuis ton poste :
```powershell
curl -I http://bibliospace.florianchague.dev
```
Doit retourner un `502 Bad Gateway` (les conteneurs ne tournent pas encore — c'est normal). Si tu as un autre code, c'est qu'nginx ne reçoit pas la requête correctement.

### 3.3 Demander le certificat Let's Encrypt

Sur la VM :
```bash
sudo certbot --nginx -d bibliospace.florianchague.dev
```

Réponds aux questions :
- Email : ton adresse mail
- Accept ToS : Yes
- Share email : No (au choix)
- Redirect HTTP → HTTPS : **2 (Redirect)** — fortement recommandé

Certbot modifie automatiquement ton `bibliospace.florianchague.dev.conf` pour ajouter les `ssl_certificate` etc.

Vérifier :
```bash
sudo nginx -t && sudo systemctl reload nginx
```

---

## Phase 4 — Démarrage des conteneurs

```bash
cd ~/bibliospace
docker compose -f docker-compose.prod.yaml --env-file .env pull
docker compose -f docker-compose.prod.yaml --env-file .env up -d
```

Suivre les logs au démarrage :
```bash
docker compose -f docker-compose.prod.yaml logs -f
```

Tu dois voir, dans cet ordre :
1. `bibliospace-db` qui démarre MySQL puis devient `healthy`
2. `bibliospace-back` qui se connecte à la DB, applique le DDL Hibernate, crée le compte admin si la table est vide, puis logge `Started LibraryApplication in X.XXX seconds`
3. `bibliospace-front` qui logge `Listening on http://[::]:3000`

Quand tout est en `Up` et le back log dit "Started", `Ctrl+C` pour sortir des logs (les conteneurs continuent en arrière-plan).

```bash
docker ps | grep bibliospace
```
Tu dois voir 3 conteneurs `Up X seconds (healthy)`.

---

## Phase 5 — Smoke tests

### 5.1 Front

Depuis un navigateur : https://bibliospace.florianchague.dev
→ Page d'accueil Nuxt s'affiche, le cadenas HTTPS est vert.

### 5.2 Back via API

Depuis ton poste :
```powershell
curl https://bibliospace.florianchague.dev/api/categories
# Doit retourner 401 Unauthorized (normal, pas d'auth) ou un JSON.
```

### 5.3 Login bibliothécaire

```powershell
curl -X POST https://bibliospace.florianchague.dev/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"florian.chague2@gmail.com\",\"motDePasse\":\"<ton mot de passe admin>\"}'
```
Doit retourner un JWT. Si oui, le bootstrap admin a fonctionné.

### 5.4 Swagger UI

https://bibliospace.florianchague.dev/swagger-ui.html
→ Page Swagger avec la liste des endpoints.

### 5.5 Front complet

Connexion via la page de login avec les identifiants admin → tu dois pouvoir naviguer dans le catalogue.

---

## Phase 6 — Maintenance courante

### Voir les logs
```bash
docker compose -f docker-compose.prod.yaml logs -f bibliospace-back
docker compose -f docker-compose.prod.yaml logs -f bibliospace-front
docker compose -f docker-compose.prod.yaml logs --tail=100 bibliospace-db
```

### Redémarrer un service
```bash
docker compose -f docker-compose.prod.yaml restart bibliospace-back
```

### Mettre à jour après modification du code
Sur ton poste : rebuild + push (Phase 1).
Sur la VM :
```bash
cd ~/bibliospace
docker compose -f docker-compose.prod.yaml --env-file .env pull
docker compose -f docker-compose.prod.yaml --env-file .env up -d
```
Le `up -d` re-crée uniquement les conteneurs dont l'image a changé.

### Sauvegarde MySQL
```bash
docker exec bibliospace-db mysqldump \
  -u root -p"$(grep MYSQL_ROOT_PASSWORD ~/bibliospace/.env | cut -d= -f2)" \
  bibliospace > ~/bibliospace/backup-$(date +%Y%m%d).sql
```

### Tout arrêter
```bash
docker compose -f docker-compose.prod.yaml down
# Pour effacer aussi les volumes (DESTRUCTIF, supprime la BDD) :
# docker compose -f docker-compose.prod.yaml down -v
```

---

## Phase 7 — Sécurisation post-déploiement

### Retirer la clé Claude des authorized_keys
```bash
sed -i '/claude-deploy-bibliospace/d' ~/.ssh/authorized_keys
cat ~/.ssh/authorized_keys  # vérifier qu'elle a disparu
```

Côté Windows, supprimer la paire de clés :
```powershell
Remove-Item $env:USERPROFILE\.ssh\bibliospace_deploy
Remove-Item $env:USERPROFILE\.ssh\bibliospace_deploy.pub
```

### Vérifier qu'aucun port autre que 80/443 n'est exposé publiquement
```bash
sudo ufw status   # si ufw est actif
sudo ss -tlnp | grep -v 127.0.0.1
```

Tu dois voir nginx sur 80 et 443, et `sshd` sur 22. Rien d'autre vers `0.0.0.0` sur des ports autres que ceux de tes apps existantes (3000, 3001, 3002, 3003, 3011 — pas 3020 ni 3021 qui doivent être en `127.0.0.1` only).

---

## Annexe — Troubleshooting

### "502 Bad Gateway" sur https://bibliospace.florianchague.dev
- Le conteneur `bibliospace-front` n'écoute pas sur `127.0.0.1:3020`. Vérifier `docker ps` et `docker logs bibliospace-front`.

### "Connection refused" entre back et DB
- La DB met du temps à devenir `healthy` au premier démarrage (init du schema). Attendre 30s puis `docker compose restart bibliospace-back`.

### Compte admin pas créé
- La table `utilisateur` n'était pas vide. Le bootstrap ne tourne que si la table est vide. Pour réinitialiser : `docker compose down -v && docker compose up -d` (PERD TOUTES LES DONNÉES).

### Certificat Let's Encrypt qui rate
- Vérifier que le DNS pointe bien sur la VM : `dig bibliospace.florianchague.dev +short`
- Vérifier que le port 80 est ouvert vers Internet (groupe de sécurité Alibaba)
- Réessayer : `sudo certbot --nginx -d bibliospace.florianchague.dev`

### OOM (Out Of Memory) sur la VM
- 2 GiB c'est tendu. Symptomes : conteneurs qui se restart en boucle, kernel kills.
- `free -m && swapon --show` pour vérifier
- Si pas de swap, en activer 2 GiB :
  ```bash
  sudo fallocate -l 2G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  ```

---

**Une fois la Phase 5 validée, fais 2-3 captures d'écran de l'app en production pour les inclure dans le mémoire et la présentation orale.**
