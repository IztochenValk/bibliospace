package com.chague.bibliotheque.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {
    private String uploadDir = "public/images/livres";
    private long maxFileSizeBytes = 5242880;
}
