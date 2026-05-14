package com.chague.bibliotheque.infrastructure.config;

import com.chague.bibliotheque.domain.Langue;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SeedBookItem {
    private String titre;
    private String auteur;
    private String description;
    private Langue langue;
    private String imageUrl;

    /**
     * ISBN-10 compact : 10 caractères (9 chiffres + clé 0-9 ou X). Peut être null.
     */
    private String isbn;

    private List<String> categories = new ArrayList<>();
}
