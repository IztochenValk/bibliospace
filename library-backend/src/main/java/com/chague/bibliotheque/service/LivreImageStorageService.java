package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.ImageUploadDto;
import com.chague.bibliotheque.infrastructure.config.FileStorageProperties;
import org.apache.tika.Tika;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class LivreImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif"
    );

    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final Tika tika = new Tika();
    private final FileStorageProperties fileStorageProperties;

    public LivreImageStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public ImageUploadDto.UploadImageResponse store(MultipartFile file) {
        validateFilePresence(file);

        String detectedContentType = detectContentType(file);
        validateContentType(detectedContentType);

        String extension = resolveExtension(file, detectedContentType);
        String randomFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path uploadDir = getUploadDir();
        Path targetFile = uploadDir.resolve(randomFileName).normalize();

        ensureTargetInsideDirectory(uploadDir, targetFile);
        createDirectoryIfNeeded(uploadDir);
        copyFile(file, targetFile);

        return new ImageUploadDto.UploadImageResponse(
                randomFileName,
                "/api/media/livres/" + randomFileName,
                file.getSize(),
                detectedContentType
        );
    }

    public Path loadAsPath(String filename) {
        validateStoredFilename(filename);

        Path uploadDir = getUploadDir();
        Path file = uploadDir.resolve(filename).normalize();

        ensureTargetInsideDirectory(uploadDir, file);

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Fichier image introuvable");
        }

        return file;
    }

    public Resource loadAsResource(String filename) {
        Path file = loadAsPath(filename);
        return new PathResource(file);
    }

    public String detectStoredContentType(Path file) {
        try {
            String probe = Files.probeContentType(file);
            if (probe != null && !probe.isBlank()) {
                return probe;
            }

            try (InputStream in = Files.newInputStream(file)) {
                String detected = tika.detect(in, file.getFileName().toString());
                if (detected != null && !detected.isBlank()) {
                    return detected;
                }
            }
        } catch (IOException ignored) {
        }

        return "application/octet-stream";
    }

    public String normalizeStoredFilename(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }

        validateStoredFilename(normalized);
        return normalized;
    }

    private Path getUploadDir() {
        return Path.of(fileStorageProperties.getUploadDir()).normalize().toAbsolutePath();
    }

    private void validateStoredFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        if (!SAFE_FILENAME.matcher(filename).matches()) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }
    }

    private void validateFilePresence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier image fourni");
        }
    }

    private String detectContentType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String detected = tika.detect(inputStream, file.getOriginalFilename());
            if (detected == null || detected.isBlank()) {
                throw new IllegalArgumentException("Type MIME de l'image indétectable");
            }
            return detected;
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier image", e);
        }
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé: " + contentType);
        }
    }

    private String resolveExtension(MultipartFile file, String detectedContentType) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null) {
            String ext = StringUtils.getFilenameExtension(originalFilename);
            if (ext != null) {
                String normalized = ext.trim().toLowerCase(Locale.ROOT);
                if (ALLOWED_EXTENSIONS.contains(normalized)) {
                    return normalized;
                }
            }
        }

        return switch (detectedContentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> throw new IllegalArgumentException("Extension impossible à déterminer");
        };
    }

    private void ensureTargetInsideDirectory(Path uploadDir, Path targetFile) {
        if (!targetFile.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Chemin de destination invalide");
        }
    }

    private void createDirectoryIfNeeded(Path uploadDir) {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier d'upload", e);
        }
    }

    private void copyFile(MultipartFile file, Path targetFile) {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier image", e);
        }
    }
}
