# ============================================================================
# Étage 1 : build Maven
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# 1. On copie d'abord pom.xml seul pour exploiter le cache Docker :
#    tant que pom.xml ne change pas, les dépendances ne sont re-téléchargées.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 2. On copie ensuite les sources et on package
COPY src ./src
RUN mvn -B clean package -DskipTests

# ============================================================================
# Étage 2 : runtime JRE (image finale légère)
# ============================================================================
FROM eclipse-temurin:17-jre-alpine

# Utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copie du jar packagé
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Dossier d'uploads en volume monté (images de livres uploadées par les
# utilisateurs).
RUN mkdir -p /uploads && chown spring:spring /uploads

# Dossier d'artefacts générés au runtime par l'app : schema.sql Hibernate,
# rapports d'audit, etc. Monté en volume Docker dans docker-compose.prod.yaml
# pour persister entre redéploiements.
# Ownership spring:spring -> le user non-root du conteneur peut y ecrire.
RUN mkdir -p /app/data /app/keys && chown spring:spring /app/data /app/keys

USER spring

EXPOSE 8080

# Healthcheck via l'endpoint actuator si présent, sinon racine
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || wget -qO- http://localhost:8080/ || exit 1

# JVM tuning : heap limitée à 384 MB pour cohabiter avec MySQL sur 2 GiB de RAM
# (surchargeable via la variable JAVA_OPTS, ex. hébergeur PaaS à 512 Mo)
ENV JAVA_OPTS="-Xmx384m -Xms128m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Clés JWT : sur la VM elles sont bind-montées dans /app/keys. Sur un PaaS sans
# montage de fichiers (SnapDeploy, Render...), on peut les passer en base64 via
# JWT_PRIVATE_PEM_B64 / JWT_PUBLIC_PEM_B64 : elles sont alors écrites au démarrage.
ENTRYPOINT ["sh", "-c", "if [ -n \"$JWT_PRIVATE_PEM_B64\" ] && [ -n \"$JWT_PUBLIC_PEM_B64\" ]; then echo \"$JWT_PRIVATE_PEM_B64\" | base64 -d > /app/keys/jwt-private.pem && echo \"$JWT_PUBLIC_PEM_B64\" | base64 -d > /app/keys/jwt-public.pem && export APP_JWT_PRIVATE_KEY_LOCATION=file:/app/keys/jwt-private.pem APP_JWT_PUBLIC_KEY_LOCATION=file:/app/keys/jwt-public.pem; fi; exec java $JAVA_OPTS -jar app.jar"]
