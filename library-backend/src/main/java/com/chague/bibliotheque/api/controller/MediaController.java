package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.LivreImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(ApiPaths.MEDIA + "/livres")
@RequiredArgsConstructor
public class MediaController {

    private final LivreImageStorageService livreImageStorageService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getLivreImage(@PathVariable String filename) {
        Path filePath = livreImageStorageService.loadAsPath(filename);
        Resource resource = livreImageStorageService.loadAsResource(filename);

        String contentType = livreImageStorageService.detectStoredContentType(filePath);
        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filePath.getFileName().toString()).build().toString()
                )
                .body(resource);
    }
}
