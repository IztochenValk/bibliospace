package com.chague.bibliotheque.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Configuration des ressources statiques servies par le back.
 *
 * <p>Mappe l'URL publique {@code /api/media/livres/<filename>} vers le dossier
 * physique {@code app.storage.upload-dir} (cf. {@link FileStorageProperties}).
 * Les couvertures de livres uploadées via {@code POST /api/livres/images/upload}
 * sont ainsi exposées en lecture seule sans nécessiter de contrôleur dédié,
 * directement par le {@code ResourceHandler} natif Spring MVC.</p>
 *
 * <p>L'accès est autorisé en lecture publique via {@link SecurityConfig} :
 * les couvertures ne contiennent aucune donnée sensible et doivent rester
 * accessibles via {@code <img src=...>} sans en-tête {@code Authorization}.</p>
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    public StaticResourceConfig(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Path.of(fileStorageProperties.getUploadDir())
                .normalize()
                .toAbsolutePath();

        // Le slash final est OBLIGATOIRE : Spring concatène le segment d'URL
        // (filename) directement à la fin de cette location pour résoudre
        // le fichier sur disque.
        registry.addResourceHandler("/api/media/livres/**")
                .addResourceLocations("file:" + uploadDir.toString() + "/");

        // Front Nuxt genere en statique (classpath:/static/), present uniquement
        // dans l'image mono-conteneur (Dockerfile a la racine). Toute route
        // inconnue hors /api renvoie index.html pour laisser le routeur Vue gerer.
        // Sans dossier static (deploiement classique), ce handler ne sert rien.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        Resource index = new ClassPathResource("static/index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}
