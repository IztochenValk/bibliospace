package com.chague.bibliotheque.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

import com.chague.bibliotheque.domain.StatutEmprunt;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "emprunt")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;

    @Column(nullable = false)
    private LocalDate dateEmprunt;

    @Column(nullable = false)
    private LocalDate dateRetourPrevue;

    @Column
    private LocalDate dateRetourEffective;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private StatutEmprunt statut;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
