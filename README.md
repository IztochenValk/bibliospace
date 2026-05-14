# BiblioSpace

> Application web de gestion d'une bibliothèque de quartier — catalogue, emprunts, gestion des utilisateurs et des rôles.
> Projet fil rouge réalisé dans le cadre du titre RNCP Concepteur Développeur d'Applications (CDA), centre ADRAR Ramonville-Saint-Agne, promotion 2025-2026.

**Démo en ligne** → [https://bibliospace.florianchague.dev](https://bibliospace.florianchague.dev)
**Documentation API (Swagger)** → [https://bibliospace.florianchague.dev/swagger-ui/index.html](https://bibliospace.florianchague.dev/swagger-ui/index.html)

[![CI/CD - Build, Push & Déploiement](https://github.com/IztochenValk/bibliospace-prod/actions/workflows/build-and-push.yml/badge.svg)](https://github.com/IztochenValk/bibliospace-prod/actions/workflows/build-and-push.yml)

---

## Stack

| Catégorie | Choix |
|---|---|
| Backend | Java 17 · Spring Boot 4 · Spring Data JPA · Spring Security OAuth2 Resource Server (JWT RSA via Nimbus) · Apache Tika |
| Frontend | TypeScript · Nuxt 4 (Vue 3 SSR) · Tailwind CSS 4 · DaisyUI 5 |
| Base de données | MySQL 8.4 |
| Tests | JUnit 5 + Mockito (unitaires) · Testcontainers (intégration MySQL réelle) · Cypress (e2e) |
| Conteneurisation | Docker multi-stage (build → JRE Alpine) · Docker Compose |
| Reverse proxy | nginx + certbot (Let's Encrypt) |
| CI/CD | GitHub Actions · GitHub Container Registry (GHCR) |
| Hébergement | VM Alibaba Cloud (Ubuntu 24.04, 2 vCPU / 2 GiB RAM) |

---

## Architecture

```
                              Internet
                                 │
                          ┌──────▼──────┐
                          │    nginx    │  Reverse proxy + TLS Let's Encrypt
                          │  (host VM)  │  bibliospace.florianchague.dev
                          └──┬───────┬──┘
                             │       │
                  /api/* + Swagger   │  Tout le reste
                             │       │
              ┌──────────────▼──┐  ┌─▼──────────────┐
              │ bibliospace-back │  │ bibliospace-   │
              │ Spring Boot 4    │  │ front          │
              │ port 8080        │  │ Nuxt 4 SSR     │
              │ (127.0.0.1:3021) │  │ port 3000      │
              └────────┬─────────┘  │ (127.0.0.1:3020)│
                       │            └─────────────────┘
                       │
            ┌──────────▼──────────┐
            │   bibliospace-db    │  MySQL 8.4
            │   port 3306         │  bind interne uniquement
            │   (réseau Docker)   │
            └─────────────────────┘
```

Les trois services tournent dans le même réseau Docker `bibliospace_net`. Seul nginx est exposé publiquement (ports 80 / 443) ; le back et le front sont bindés sur `127.0.0.1` de la VM, et la base de données n'est accessible qu'au sein du réseau Docker.

L'authentification utilise un JWT signé en RSA (clé privée pour l'émission, clé publique pour la vérification), avec une durée de vie de 60 minutes et l'API stateless (`SessionCreationPolicy.STATELESS`). Les clés sont injectées au runtime via bind mount et ne sont jamais embarquées dans l'image Docker.

Le détail de l'architecture applicative (couches `api / domain / service / infrastructure`, `@RestControllerAdvice` par domaine, `@PreAuthorize` sur les services, `AuthorizationService` métier) est dans le mémoire (sections 6 et 7).

---

## Démarrage local

Prérequis : Docker et Docker Compose v2.

```bash
git clone https://github.com/IztochenValk/bibliospace-prod.git
cd bibliospace-prod

# Lance les 3 services (DB + back + front)
cd library-backend
docker compose up -d

# Dans un autre terminal
cd library-frontend
docker compose up -d
```

Une fois les conteneurs healthy :

| Service | URL locale |
|---|---|
| Frontend Nuxt | http://localhost:3000 |
| Backend Spring | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| MySQL | localhost:3306 (utilisateur `root` / `root` en dev) |

Le seed automatique au premier boot crée des utilisateurs de démonstration :

| Rôle | Email | Mot de passe (dev local uniquement) |
|---|---|---|
| Administrateur | `admin@library.local` | `Admin123!` |
| Bibliothécaire | `biblio@library.local` | `Biblio123!` |
| Adhérent | `adherent@library.local` | `User12345!` |

> En production, ces mots de passe sont écrasés par les variables d'environnement `APP_DEMO_PASSWORD_*` injectées via les secrets GitHub Actions (voir [deploy/CI-CD.md](deploy/CI-CD.md)).

---

## Déploiement en production

Le déploiement est **entièrement automatisé** via GitHub Actions. Un push sur `main` qui touche `library-backend/`, `library-frontend/`, `deploy/` ou `.github/workflows/` déclenche le pipeline complet.

### Vue d'ensemble

```
push main ──┐
            ▼
       ┌─────────┐
       │ prepare │  (namespace GHCR en minuscules)
       └────┬────┘
            │
   ┌────────┴────────┐
   ▼                 ▼
┌──────────┐   ┌──────────┐
│  build-  │   │  build-  │   Build multi-stage Docker
│ backend  │   │ frontend │   Push GHCR avec tags :latest et :sha
└────┬─────┘   └─────┬────┘
     │               │
     └───────┬───────┘
             ▼
        ┌─────────┐
        │ deploy  │   SSH + rsync deploy/ + génère .env
        └────┬────┘   injecte clés RSA via stdin
             │        docker compose pull + up -d
             ▼        smoke tests (polling jusqu'à 120s)
       Production
       en ligne
```

**Durée totale typique** : 2 à 4 minutes (cache GitHub Actions chaud), jusqu'à 6 minutes en cold start.

### Étapes en bref

1. **Build des images Docker** parallèles pour le back (Spring Boot 4) et le front (Nuxt 4), avec cache GitHub Actions par scope. Les images sont taguées `:latest` et `:<sha>` pour traçabilité et rollback.
2. **Push GHCR** sur `ghcr.io/iztochenvalk/bibliospace-back` et `ghcr.io/iztochenvalk/bibliospace-front` avec labels OCI (source, revision, title).
3. **Déploiement SSH** sur la VM Alibaba :
   - Synchronisation du dossier `deploy/` via `rsync` (compose prod, nginx, setup-vm.sh).
   - Génération du `.env` depuis les secrets GitHub Actions (mode 600).
   - Injection des clés RSA JWT via stdin (jamais en argument de commande, jamais dans les logs).
   - Exécution de `setup-vm.sh` (idempotent : Docker, nginx, reload conditionnel).
   - `docker login ghcr.io` côté VM avec un PAT scope `read:packages`.
   - `docker compose pull && up -d --force-recreate` pour redémarrer la stack.
4. **Smoke tests** : polling actif jusqu'à 120 s sur `https://bibliospace.florianchague.dev/v3/api-docs` puis vérification du front en HTTPS.

La documentation détaillée du pipeline (chaque étape, chaque secret, dépannage typique) est dans [**deploy/CI-CD.md**](deploy/CI-CD.md).

### Secrets requis (GitHub → Settings → Secrets and variables → Actions)

| Catégorie | Secret | Usage |
|---|---|---|
| Accès VM | `SSH_PRIVATE_KEY` · `SSH_HOST` · `SSH_USER` | Connexion SSH au compte applicatif `deploy-bibliospace` |
| Base de données | `MYSQL_ROOT_PASSWORD` · `MYSQL_PASSWORD` | Comptes MySQL |
| Seed users | `DEMO_PASSWORD_ADMIN` · `DEMO_PASSWORD_BIBLIOTHECAIRE` · `DEMO_PASSWORD_ADHERENT` | Override des passwords des 3 rôles seed |
| Registry | `GHCR_READ_TOKEN` | PAT classic scope `read:packages` (pull GHCR depuis la VM) |
| JWT signing | `APP_JWT_PRIVATE_KEY` · `APP_JWT_PUBLIC_KEY` | Clés RSA PEM (multi-ligne) |

Le fichier d'exemple [`deploy/.env.example`](deploy/.env.example) liste les variables consommées côté VM avec des commandes `openssl rand -base64 N` pour générer des valeurs sûres.

### Déploiement initial (à faire une fois)

Pour onboarder une nouvelle VM, le script idempotent [`deploy/setup-vm.sh`](deploy/setup-vm.sh) installe Docker, recharge la conf nginx ([`deploy/nginx/bibliospace.florianchague.dev.conf`](deploy/nginx/bibliospace.florianchague.dev.conf)), et avertit si le certificat Let's Encrypt n'est pas encore généré. Voir [`deploy/RUNBOOK.md`](deploy/RUNBOOK.md) pour la check-list de mise en route.

### Rollback rapide

Sur la VM :
```bash
cd ~/bibliospace
sed -i 's/IMAGE_TAG=latest/IMAGE_TAG=<sha-précédent>/' .env
docker compose -f docker-compose.prod.yaml --env-file .env pull
docker compose -f docker-compose.prod.yaml --env-file .env up -d
```

Procédure détaillée dans [deploy/CI-CD.md](deploy/CI-CD.md#comment-rollback-en-cas-de-déploiement-cassé).

---

## Structure du repo

```
bibliospace-prod/
├── .github/
│   └── workflows/
│       └── build-and-push.yml      Pipeline CI/CD complet
│
├── library-backend/                Backend Spring Boot 4 / Java 17
│   ├── src/main/java/com/chague/bibliotheque/
│   │   ├── api/                    Controllers, DTOs, ControllerAdvice par domaine
│   │   ├── domain/                 Entités JPA + énumérations
│   │   ├── infrastructure/         Config (security, JWT, persistence)
│   │   └── service/                Logique métier (avec @PreAuthorize)
│   ├── src/test/java/              Tests unitaires (JUnit + Mockito)
│   ├── Dockerfile                  Multi-stage Maven → JRE Alpine
│   └── pom.xml
│
├── library-frontend/               Frontend Nuxt 4 / Vue 3 / TS
│   ├── components/                 Composants UI par domaine
│   ├── composables/
│   │   ├── pages/                  Logique d'état par page
│   │   ├── forms/                  Helpers de formulaire (ISBN, upload, sync)
│   │   ├── auth/                   Guards par rôle
│   │   └── ui/                     useToast, etc.
│   ├── pages/                      Routes auto-générées par Nuxt
│   ├── services/                   Couche d'appels HTTP (un fichier par domaine)
│   ├── types/                      Miroirs TS des DTOs Java
│   ├── utils/                      Mappers + helpers d'erreurs centralisés
│   ├── cypress/                    Tests e2e
│   └── Dockerfile                  Multi-stage Node 22 → Nitro
│
└── deploy/                         Tout ce qui touche à la production
    ├── docker-compose.prod.yaml    Orchestration des 3 services
    ├── .env.example                Template des variables d'env (à copier en .env)
    ├── setup-vm.sh                 Bootstrap idempotent de la VM
    ├── nginx/                      Conf reverse proxy + TLS
    ├── CI-CD.md                    Documentation complète du pipeline
    ├── RUNBOOK.md                  Opérations courantes en prod
    └── STORAGE-STRATEGY.md         Stratégie de persistance (volumes, backups)
```

---

## Sécurité (résumé)

- **JWT RSA stateless** émis avec `JwtEncoder` (Nimbus), validé par `JwtDecoder`. Clés en bind mount au runtime, jamais dans l'image.
- **BCrypt** (cost factor 10) sur tous les mots de passe utilisateurs.
- **Aucun secret en clair** dans le repo, l'historique git, les logs CI/CD ou les images Docker. Les secrets transitent uniquement via `secrets:` GitHub Actions → `env:` du step → stdin distant.
- **CORS** strict (liste blanche d'origines), **CSRF** désactivé (API stateless, JWT en `Authorization` header).
- **Validation Jakarta** sur tous les DTOs avec `@RestControllerAdvice` par domaine qui transforme les exceptions custom en réponse HTTP unifiée (`ApiErrorFactory`).
- **Autorisation par méthode** via `@EnableMethodSecurity` + `@PreAuthorize`, complété par un `AuthorizationService` pour les règles métier transverses (un adhérent ne voit que ses propres emprunts, un bibliothécaire ne gère que des adhérents, etc.).
- **Upload images** : détection MIME via Apache Tika sur le contenu réel (pas l'extension), renommage UUID, taille max 5 MiB, path traversal bloqué par normalisation et vérification d'appartenance au dossier cible.
- **Conteneurs non-root** : user `spring` (UID 100) côté back, user `nuxt` côté front.
- **Réseau** : la base de données n'a aucun port mappé sur le host, et le back/front ne sont bindés que sur `127.0.0.1`. Seul nginx est exposé publiquement.

---

## Contexte projet

Projet fil rouge de mémoire CDA (titre RNCP Concepteur Développeur d'Applications). Développé en solo sur deux mois (mars-avril 2026) en parallèle d'un stage en entreprise. Le mémoire de soutenance détaille les choix de conception, l'analyse fonctionnelle, l'architecture logicielle, la stratégie de tests et la chaîne DevOps.

**Auteur** : Florian Chague — promotion 2025-2026, ADRAR Ramonville.

---

## Licence

Code libre d'utilisation pour relecture pédagogique et démonstration. Voir [LICENSE](LICENSE).
