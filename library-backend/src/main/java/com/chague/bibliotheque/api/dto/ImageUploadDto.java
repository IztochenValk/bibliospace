package com.chague.bibliotheque.api.dto;

public final class ImageUploadDto {

    private ImageUploadDto() {
    }

    public record UploadImageResponse(
            String fileName,
            String imageUrl,
            long size,
            String contentType
    ) {
    }
}
