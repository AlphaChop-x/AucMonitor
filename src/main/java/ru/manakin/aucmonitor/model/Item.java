package ru.manakin.aucmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table
@Entity
@Setter
@Getter
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

    @Column(length = 1000)
    private String description;

    @Column
    private float weight;

    @Column
    @Enumerated(EnumType.STRING)
    private ColorEnum color;

    @JsonIgnore
    @ManyToMany(mappedBy = "favoriteItems")
    private Set<AppUser> appUser = new HashSet<>();
}
