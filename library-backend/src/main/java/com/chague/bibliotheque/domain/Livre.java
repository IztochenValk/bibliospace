package com.chague.bibliotheque.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor   // ← ajouter ici
@Entity
@Table(name="livre")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable=false)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String auteur;

    @ManyToMany
    @JoinTable(
            name = "livre_categorie",
            joinColumns = @JoinColumn(name = "livre_id"),
            inverseJoinColumns = @JoinColumn(name = "categorie_id")
    )
    private List<Categorie> categories = new ArrayList<Categorie>();

    @Column(nullable = true, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Langue langue;

    @Column(
            name = "image_url",
            nullable = true,
            length = 500
    )
    private String imageUrl;

    /**
     * ISBN-10 stocké sous forme compacte (10 caractères : 9 chiffres + clé de contrôle 0-9 ou X).
     * Champ nullable, la saisie reste facultative côté formulaire. Aucune incidence côté logique métier.
     */
    @Column(length = 10)
    private String isbn;

    @Column(name = "quantite_totale", nullable = false)
    private Integer quantiteTotale = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;


}
