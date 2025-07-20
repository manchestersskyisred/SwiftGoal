package com.swiftgoal.app.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "players", indexes = {
    @Index(name = "idx_players_full_name", columnList = "full_name")
})
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "known_as")
    private String knownAs;

    @Column(nullable = false)
    private String nationality;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String position;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "photo_url", length = 1024)
    private String photoUrl;
} 