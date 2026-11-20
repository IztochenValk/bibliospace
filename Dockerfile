# ============================================================================
# BiblioSpace - image "demo" mono-conteneur (front + back + base H2 en memoire)
#
# Pensee pour un hebergement gratuit sans base de donnees ni reseau interne
# (SnapDeploy, Render...) : une seule URL, le front Nuxt est genere en statique
# et servi par Spring Boot, l'API reste sur /api. La base H2 est recreee a
# chaque demarrage par les seeders (livres + comptes de demo).
#
# La stack de production (MySQL + 2 conteneurs) reste dans deploy/ et les
# Dockerfile de library-backend/ et library-frontend/, inchanges.
#
# Variables utiles au runtime :
#   SPRING_DATASOURCE_URL=jdbc:h2:mem:bibliospace;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
#   JWT_PRIVATE_PEM_B64 / JWT_PUBLIC_PEM_B64 : cles RSA en base64
# ============================================================================

# ---- Etage 1 : front Nuxt en SPA statique ----------------------------------
FROM node:22-alpine AS front
WORKDIR /front
COPY library-frontend/package.json library-frontend/package-lock.json* ./
# CYPRESS_INSTALL_BINARY=0 : pas besoin du binaire Cypress (~200 Mo) pour builder
RUN CYPRESS_INSTALL_BINARY=0 npm ci
COPY library-frontend/ ./
# apiBase vide -> URLs relatives /api/... (meme origine que le back)
ENV NUXT_SSR=false NUXT_PUBLIC_API_BASE=""
RUN npx nuxt generate

# ---- Etage 2 : back Spring Boot avec le front embarque ---------------------
FROM maven:3.9.9-eclipse-temurin-17 AS back
WORKDIR /app
COPY library-backend/pom.xml .
RUN mvn -B dependency:go-offline
COPY library-backend/src ./src
COPY --from=front /front/.output/public ./src/main/resources/static
RUN mvn -B clean package -DskipTests

# ---- Etage 3 : runtime ------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=back --chown=spring:spring /app/target/*.jar app.jar
RUN mkdir -p /uploads/livres /app/data /app/keys && chown -R spring:spring /uploads /app/data /app/keys
USER spring
EXPOSE 8080

ENV SERVER_PORT=8080 \
    SPRING_DATASOURCE_URL="jdbc:h2:mem:bibliospace;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver \
    SPRING_DATASOURCE_USERNAME=sa \
    SPRING_DATASOURCE_PASSWORD="" \
    SPRING_JPA_SHOW_SQL=false \
    APP_STORAGE_UPLOAD_DIR=/uploads/livres \
    JAVA_OPTS="-Xmx300m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- http://localhost:8080/ || exit 1

ENTRYPOINT ["sh", "-c", "if [ -n \"$JWT_PRIVATE_PEM_B64\" ] && [ -n \"$JWT_PUBLIC_PEM_B64\" ]; then echo \"$JWT_PRIVATE_PEM_B64\" | base64 -d > /app/keys/jwt-private.pem && echo \"$JWT_PUBLIC_PEM_B64\" | base64 -d > /app/keys/jwt-public.pem && export APP_JWT_PRIVATE_KEY_LOCATION=file:/app/keys/jwt-private.pem APP_JWT_PUBLIC_KEY_LOCATION=file:/app/keys/jwt-public.pem; fi; exec java $JAVA_OPTS -jar app.jar"]
