# ============================================================================
# Étage 1 : install des dépendances + build Nuxt (SSR)
# ============================================================================
FROM node:22-alpine AS build

WORKDIR /app

# Copie les manifestes en premier pour exploiter le cache Docker
COPY package.json package-lock.json* ./

# Installation reproductible
RUN npm ci

# Copie du code source
COPY . .

# Build Nuxt en mode SSR (production)
RUN npm run build

# ============================================================================
# Étage 2 : runtime Nitro (image finale légère)
# ============================================================================
FROM node:22-alpine

# Utilisateur non-root
RUN addgroup -S nuxt && adduser -S nuxt -G nuxt

WORKDIR /app

# Copie uniquement la sortie buildée (.output contient tout ce qu'il faut)
COPY --from=build --chown=nuxt:nuxt /app/.output ./.output

USER nuxt

# Nuxt Nitro écoute sur le port 3000 par défaut
EXPOSE 3000

ENV NODE_ENV=production
ENV HOST=0.0.0.0
ENV PORT=3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -qO- http://localhost:3000/ || exit 1

CMD ["node", ".output/server/index.mjs"]
