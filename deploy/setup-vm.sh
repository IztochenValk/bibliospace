#!/usr/bin/env bash
# =============================================================================
# setup-vm.sh — bootstrap idempotent de la VM BiblioSpace
#
# Ce script est exécuté à chaque déploiement, mais ne fait que ce qui est
# strictement nécessaire (idempotence). Il :
#   - vérifie les pré-requis (docker, nginx, ufw)
#   - installe / met à jour le site nginx bibliospace.florianchague.dev
#     uniquement si le contenu diffère
#   - recharge nginx UNIQUEMENT si le site a changé
#   - n'installe PAS les certificats Let's Encrypt (à faire manuellement
#     une seule fois via : sudo certbot --nginx -d bibliospace.florianchague.dev)
#
# Pré-requis admin sudo NOPASSWD à configurer une fois :
#     sudo visudo
#     # ajouter à la fin :
#     admin ALL=(ALL) NOPASSWD: /usr/bin/cp, /usr/bin/ln, /usr/sbin/nginx, /bin/systemctl reload nginx
#
# =============================================================================

set -euo pipefail

DEPLOY_DIR="$HOME/bibliospace"
NGINX_SITE_NAME="bibliospace.florianchague.dev"
NGINX_SITE_SRC="${DEPLOY_DIR}/nginx/${NGINX_SITE_NAME}.conf"
NGINX_SITE_DST="/etc/nginx/sites-available/${NGINX_SITE_NAME}"
NGINX_SITE_ENABLED="/etc/nginx/sites-enabled/${NGINX_SITE_NAME}"

log() { echo "[setup-vm] $*"; }
warn() { echo "[setup-vm] WARN: $*" >&2; }
err() { echo "[setup-vm] ERROR: $*" >&2; exit 1; }

# -----------------------------------------------------------------------------
# 1. Vérification des pré-requis
# -----------------------------------------------------------------------------
log "Vérification des pré-requis..."

command -v docker > /dev/null || err "Docker n'est pas installé"
command -v nginx  > /dev/null || err "nginx n'est pas installé"

if ! [ -f "${NGINX_SITE_SRC}" ]; then
    err "Site nginx source introuvable : ${NGINX_SITE_SRC}"
fi

# -----------------------------------------------------------------------------
# 2. Vérification du .env (sécurité minimale, on ne le génère pas ici —
#    c'est le workflow GitHub Actions qui s'en charge avec les secrets)
# -----------------------------------------------------------------------------
if ! [ -f "${DEPLOY_DIR}/.env" ]; then
    err ".env manquant dans ${DEPLOY_DIR}/. Le workflow doit l'avoir généré."
fi

# Vérification que les variables critiques sont renseignées et non vides
required_vars=(
    "MYSQL_ROOT_PASSWORD"
    "MYSQL_PASSWORD"
    "APP_DEMO_PASSWORD_ADMIN"
    "APP_DEMO_PASSWORD_BIBLIOTHECAIRE"
    "APP_DEMO_PASSWORD_ADHERENT"
)
missing_vars=()
for var in "${required_vars[@]}"; do
    if ! grep -qE "^${var}=.+" "${DEPLOY_DIR}/.env"; then
        missing_vars+=("${var}")
    fi
done

if [ ${#missing_vars[@]} -gt 0 ]; then
    err "Variables manquantes ou vides dans .env : ${missing_vars[*]}"
fi
log ".env OK (toutes les variables critiques sont renseignées)"

# -----------------------------------------------------------------------------
# 3. Installation / mise à jour du site nginx (idempotent)
# -----------------------------------------------------------------------------
nginx_changed=0

# Compare le source au dest existant ; n'écrit que si différent
if ! [ -f "${NGINX_SITE_DST}" ] || ! cmp -s "${NGINX_SITE_SRC}" "${NGINX_SITE_DST}"; then
    log "Installation du site nginx ${NGINX_SITE_NAME}..."
    sudo cp "${NGINX_SITE_SRC}" "${NGINX_SITE_DST}"
    nginx_changed=1
else
    log "Site nginx déjà à jour, aucune action."
fi

# Active le site si pas déjà fait
if ! [ -L "${NGINX_SITE_ENABLED}" ]; then
    log "Activation du site nginx (création du lien symbolique)..."
    sudo ln -s "${NGINX_SITE_DST}" "${NGINX_SITE_ENABLED}"
    nginx_changed=1
fi

# Test config nginx ; si KO on ne reload pas (sinon on casse les autres sites)
if [ ${nginx_changed} -eq 1 ]; then
    log "Test de la config nginx..."
    if sudo nginx -t 2>&1; then
        log "Reload nginx..."
        sudo systemctl reload nginx
    else
        err "Config nginx invalide, reload annulé. Inspecte /etc/nginx/sites-available/${NGINX_SITE_NAME} manuellement."
    fi
fi

# -----------------------------------------------------------------------------
# 4. Vérification HTTPS (avertissement si certbot pas encore lancé)
# -----------------------------------------------------------------------------
if ! [ -f "/etc/letsencrypt/live/${NGINX_SITE_NAME}/fullchain.pem" ]; then
    warn "Certificat Let's Encrypt absent pour ${NGINX_SITE_NAME}."
    warn "Lance manuellement (une seule fois) :"
    warn "    sudo certbot --nginx -d ${NGINX_SITE_NAME}"
    warn "Le déploiement continue, mais le site ne sera accessible qu'en HTTP."
fi

log "Setup VM terminé avec succès."
