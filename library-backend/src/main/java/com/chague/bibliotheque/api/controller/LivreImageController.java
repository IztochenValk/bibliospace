package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.ImageUploadDto;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.LivreImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(ApiPaths.LIVRES + "/images")
public class LivreImageController {

    private final LivreImageStorageService livreImageStorageService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadDto.UploadImageResponse upload(
            @RequestPart("file") MultipartFile file
    ) {
        return livreImageStorageService.store(file);
    }
}
