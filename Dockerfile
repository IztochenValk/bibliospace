# Image demo mono-conteneur : front Nuxt statique servi par Spring Boot + H2 en memoire.
# La stack de prod (MySQL) reste dans deploy/.
FROM node:22-alpine AS front
WORKDIR /front
COPY library-frontend/package*.json ./
RUN CYPRESS_INSTALL_BINARY=0 npm ci --no-audit --no-fund
COPY library-frontend/ ./
RUN NUXT_SSR=false NUXT_PUBLIC_API_BASE="" npx nuxt generate

FROM maven:3.9.9-eclipse-temurin-17 AS back
WORKDIR /app
COPY library-backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY library-backend/src ./src
COPY --from=front /front/.output/public ./src/main/resources/static
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache openssl && adduser -S -G nogroup app
WORKDIR /app
COPY --from=back /app/target/*.jar app.jar
COPY start.sh .
RUN mkdir -p /uploads/livres keys && chown -R app /uploads /app
USER app
EXPOSE 8080
ENV SERVER_PORT=8080 \
    SPRING_DATASOURCE_URL="jdbc:h2:mem:bibliospace;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver \
    SPRING_DATASOURCE_USERNAME=sa \
    SPRING_JPA_SHOW_SQL=false \
    APP_STORAGE_UPLOAD_DIR=/uploads/livres \
    JAVA_OPTS="-Xmx300m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"
CMD ["sh", "start.sh"]
