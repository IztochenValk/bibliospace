package com.chague.bibliotheque.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
public enum StatutEmprunt {
    EN_COURS("Emprunt en cours"),
    RETOURNE("Livre retourné");

    private final String label;

    //@Setter ne marche pas sur une enum.
    StatutEmprunt(String label) {
        this.label = label;
    }

}