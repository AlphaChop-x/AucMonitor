package ru.manakin.aucmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


/**
 * Сущность предмета в системе.
 * Представляет предмет.
 * <p>
 * Поле {@code apiId} используется для взаимодействия с внешним STALCRAFT API.
 * </p>
 *
 * @author Manakin A.S
 * @version 1.0
 * @since 2025-07-02
 */
@Table
@Entity
@Setter
@Getter
public class Item {
    /**
     * Уникальный идентификатор предмета в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор предмета, используемый для работы с внешним STALCRAFT API.
     */
    @Column
    private String apiId;

    /**
     * Название предмета.
     */
    @Column
    private String name;

    /**
     * Категория предмета.
     * Используется перечисление {@link CategoryEnum}.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private CategoryEnum category;

    /**
     * Подкатегория предмета.
     * Используется перечисление {@link SubcategoryEnum}.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private SubcategoryEnum subCategory;

    /**
     * Описание предмета.
     * Максимальная длина — 1000 символов.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Вес предмета.
     */
    @Column
    private float weight;

    /**
     * Цвет-редкость предмета.
     * Используется перечисление {@link ColorEnum}.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private ColorEnum color;

    /**
     * Пользователи, у которых данный предмет находится в избранном.
     * Связь многие-ко-многим с сущностью {@link AppUser}.
     * Помечено {@link JsonIgnore}, чтобы избежать рекурсии при сериализации JSON.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "favoriteItems")
    private Set<AppUser> appUser = new HashSet<>();
}
