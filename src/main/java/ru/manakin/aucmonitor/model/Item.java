package ru.manakin.aucmonitor.model;

import jakarta.persistence.*;

@Table
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String apiId;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private CategoryEnum category;

    @Column
    @Enumerated(EnumType.STRING)
    private SubcategoryEnum subCategory;

    @Column
    @Enumerated(EnumType.STRING)
    private RarityEnum rarity;

    @Column
    private String description;

    @Column
    private float weight;

    @Column
    @Enumerated(EnumType.STRING)
    private ColorEnum color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser appUser;
}
