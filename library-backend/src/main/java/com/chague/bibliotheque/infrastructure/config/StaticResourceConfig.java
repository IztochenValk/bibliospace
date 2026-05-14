package com.chague.bibliotheque.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    }
}
