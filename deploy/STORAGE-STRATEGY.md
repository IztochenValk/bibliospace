# Stratégie de stockage des conteneurs BiblioSpace

> Document de référence pour comprendre **où** et **comment** les données
> sont stockées dans la stack Docker de production. Lecture posée recommandée.

---

## 1. Le problème de base

Un conteneur Docker est par défaut **éphémère** : tout ce qui y est écrit est
perdu à son redémarrage. Pour un serveur web stateless ce n'est pas grave,
mais dès qu'une application doit **persister** des données, il faut sortir
ces données du conteneur.

Dans BiblioSpace on a 3 cas de persistance distincts :

| Cas | Données concernées | Pourquoi persister |
|-----|--------------------|--------------------|
| **Base de données** | Tables MySQL (livres, utilisateurs, emprunts, etc.) | Sinon on perd toutes les données à chaque déploiement |
| **Uploads utilisateurs** | Images des couvertures de livres | Sinon les images uploadées par les bibliothécaires disparaissent |
| **Artefacts générés au runtime** | `schema.sql` Hibernate (DDL généré au boot) | Pour audit / documentation, mais pas critique |

Docker propose **3 mécanismes** pour gérer cette persistance. C'est là qu'il
faut choisir.

---

## 2. Les 3 types de stockage Docker

### 2.1 Volume anonyme

Déclaré dans le `Dockerfile` avec `VOLUME /chemin`. Docker crée un volume
temporaire à chaque `docker run`, nommé avec un hash aléatoire.

**À éviter** : difficile à identifier, à sauvegarder, à réutiliser. On ne
sait jamais lequel est lequel quand on regarde `docker volume ls`.

On n'utilise **pas** de volumes anonymes dans BiblioSpace.

### 2.2 Volume nommé (named volume)

Déclaré dans le `docker-compose.yaml` avec un nom explicite, exemple :

```yaml
services:
  bibliospace-back:
    volumes:
      - bibliospace_data:/app/data    # nom_du_volume : chemin_dans_conteneur

volumes:
  bibliospace_data:                    # déclaration du volume
```

Docker stocke les fichiers physiquement dans `/var/lib/docker/volumes/<nom>/_data/`
sur la VM, mais **c'est Docker qui gère ce dossier**. Tu n'es pas censé y
toucher directement.

**Avantages** :
- Docker gère les permissions tout seul. Quand le volume est créé pour la
  première fois, il **hérite des permissions** du dossier dans l'image
  (donc, si dans le Dockerfile on a `chown spring:spring /app/data`, le
  volume sera créé avec ces mêmes permissions).
- Pas de conflit entre l'UID utilisé dans le conteneur et l'UID des
  utilisateurs sur la VM.
- Portable : marche pareil quel que soit le système host.

**Inconvénients** :
- Pour récupérer un fichier depuis la VM, il faut soit `docker cp`, soit
  fouiller dans `/var/lib/docker/volumes/.../`.
- Pas adapté si tu veux **éditer manuellement** des fichiers depuis la VM
  (par exemple un fichier de config que tu modifies à chaud).

### 2.3 Bind mount

C'est un **lien direct** entre un dossier de la VM et un dossier dans le
conteneur :

```yaml
services:
  bibliospace-back:
    volumes:
      - ./data:/app/data    # ./data sur la VM ↔ /app/data dans le conteneur
```

Le `./data` est relatif au dossier d'où tu lances `docker compose`, donc
ici ce serait `~/bibliospace/data/`.

**Avantages** :
- Tu vois et tu modifies les fichiers directement depuis la VM avec `cat`,
  `vim`, `scp`, etc.
- Idéal pour des fichiers que tu veux versionner ou partager.

**Inconvénients** :
- **Problème de permissions** : le conteneur a son propre user (chez nous
  `spring` UID 100), la VM a son propre user (chez nous `deploy-bibliospace`
  UID inconnu mais probablement 1001+). Quand le conteneur écrit, il essaie
  d'écrire en tant que UID 100. Si le dossier sur la VM appartient à
  l'utilisateur 1001, le conteneur n'a pas le droit d'écrire dedans.
- Solutions possibles : `chmod 777` (laxiste), aligner les UID dans le
  Dockerfile (rigide), ou run le conteneur avec un autre user (complexe).

---

## 3. Choix fait pour BiblioSpace

Pour chaque cas de persistance, voilà le choix retenu et pourquoi :

| Cas | Choix | Raison |
|-----|-------|--------|
| Base de données MySQL | **Volume nommé** `bibliospace_db_data` | Pas besoin d'éditer les fichiers MySQL à la main, Docker gère les perms (l'image MySQL utilise un user `mysql` UID 999 — un bind mount serait pénible). |
| Uploads (images de livres) | **Volume nommé** `bibliospace_uploads` | Idem : c'est l'app qui écrit, pas l'humain. Et les uploads sont accédés via l'API, pas via le filesystem. |
| `schema.sql` Hibernate | **Volume nommé** `bibliospace_data` | Choix discuté ci-dessous. |

### 3.1 Pourquoi un volume nommé pour `schema.sql` plutôt qu'un bind mount ?

Le besoin initial était : **faire atterrir `schema.sql` dans le dossier
projet sur la VM** (`~/bibliospace/data/schema.sql`) pour pouvoir le lire
sans entrer dans le conteneur.

La tentation logique serait de faire un bind mount :
```yaml
volumes:
  - ./data:/app/data
```

Mais ça rentre dans le **piège des permissions** :
- Le user du conteneur (`spring`) a UID 100.
- Le user de la VM (`deploy-bibliospace`) a un UID différent (probablement 1002+).
- Quand `spring` essaie d'écrire dans `/app/data` (= `~/bibliospace/data` sur
  la VM), il se heurte à un dossier qui appartient à `deploy-bibliospace`
  → **Permission denied**, Spring Boot crash, 502.

Pour contourner, il faudrait soit :
- `chmod 777` sur `~/bibliospace/data` (permissif, mauvaise pratique)
- Recompiler le Dockerfile avec un UID `spring` qui matche
  `deploy-bibliospace` (rigide, casse si on change de user VM)
- Faire tourner le conteneur en `--user 1001:1001` (le JAR doit accepter
  cet UID, complexe à gérer)

Aucune de ces options n'est jolie. Donc on prend le **volume nommé** :
- Docker initialise le volume avec les permissions de `/app/data` dans
  l'image (`spring:spring` grâce au `chown` dans le Dockerfile)
- Spring peut écrire sans souci
- L'extraction du fichier vers le filesystem de la VM se fait en une
  commande à la demande

### 3.2 Comment je récupère mon `schema.sql` ?

Sur la VM, depuis le dossier projet :

```bash
cd ~/bibliospace
mkdir -p data
docker cp bibliospace-back:/app/data/schema.sql data/schema.sql
cat data/schema.sql | head -30
```

Le fichier atterrit dans `~/bibliospace/data/schema.sql`. Tu peux le lire,
le copier ailleurs, l'inclure dans ton mémoire en annexe.

Pour le récupérer **sur ton poste Windows** :

```powershell
scp -i $env:USERPROFILE\.ssh\bibliospace_deploy `
    deploy-bibliospace@8.209.91.124:~/bibliospace/data/schema.sql `
    .\schema.sql
```

(Note les **backticks** pour la continuation de ligne en PowerShell.)

---

## 4. Récap des volumes BiblioSpace

```yaml
# docker-compose.prod.yaml
volumes:
  bibliospace_db_data:    # Données MySQL (tables, indexes, etc.)
  bibliospace_uploads:    # Images des couvertures de livres uploadées
  bibliospace_data:       # Artefacts générés au runtime (schema.sql)
```

Sur la VM, après le premier déploiement, tu verras :

```bash
docker volume ls | grep biblio
# DRIVER    VOLUME NAME
# local     bibliospace_bibliospace_db_data
# local     bibliospace_bibliospace_uploads
# local     bibliospace_bibliospace_data
```

Le double préfixe `bibliospace_bibliospace_` vient du fait que Docker
préfixe le nom du volume avec le nom du projet (le nom du dossier où
tourne `docker compose`, donc `bibliospace`).

---

## 5. Sauvegarde des volumes (à savoir pour la défense)

Pour un déploiement vraiment "production", il faudrait **sauvegarder
régulièrement** ces volumes — surtout le DB et les uploads. Une stratégie
type serait :

```bash
# Backup quotidien (à mettre dans un cron VM)
docker run --rm \
  -v bibliospace_bibliospace_db_data:/data:ro \
  -v ~/backups:/backup \
  alpine tar czf /backup/db-$(date +%Y%m%d).tar.gz -C /data .
```

Ce n'est pas implémenté dans BiblioSpace v1 (manque de temps), mais c'est
une amélioration légitime à mentionner en "axes d'amélioration" dans le
mémoire.

---

## 6. Pour la défense devant le jury

Si on te pose la question **"Pourquoi des volumes nommés et pas des bind
mounts ?"**, voilà la réponse courte :

> J'ai privilégié les volumes nommés pour la persistance des données et
> des artefacts générés par l'application, parce qu'ils découplent
> complètement la stratégie de stockage de la stratégie d'utilisateurs
> du système hôte. Le user `spring` UID 100 du conteneur écrit sans
> conflit, peu importe quel user UID est utilisé côté VM. Un bind mount
> aurait nécessité soit un alignement rigide des UIDs entre conteneur et
> hôte, soit des permissions permissives type `chmod 777` que je voulais
> éviter en environnement multi-applicatif. L'extraction des artefacts
> vers le filesystem de la VM se fait à la demande via `docker cp`,
> ce qui est suffisant pour le besoin (consultation occasionnelle, pas
> d'édition concurrente).

C'est une réponse précise, qui montre que tu as **conscience des
trade-offs**, et qui est exactement le niveau attendu en CDA.
