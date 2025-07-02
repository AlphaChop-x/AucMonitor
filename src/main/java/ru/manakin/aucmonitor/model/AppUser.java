package ru.manakin.aucmonitor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Сущность пользователя приложения.
 * Реализует интерфейс {@link UserDetails} для интеграции с Spring Security.
 * Содержит основную информацию о пользователе, включая имя, email, пароль, роль и избранные предметы.
 *
 * @author Manakin A.S
 * @version 1.0
 * @since 2025-07-02
 */
@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AppUser implements UserDetails {
    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя. Не может быть пустым.
     */
    @NotBlank(message = "Имя не должно быть пустым")
    @Column
    private String username;

    /**
     * Электронная почта пользователя.
     * Должна быть валидным email и не пустой.
     */
    @NotBlank(message = "Почта не должна быть пустой")
    @Email(message = "Некорректный email")
    @Column
    private String email;

    /**
     * Пароль пользователя.
     * Минимальная длина — 8 символов.
     */
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    @Column
    private String password;

    /**
     * Роль пользователя в системе.
     * Хранится в виде строки, соответствует {@link RoleEnum}.
     * По умолчанию — {@link RoleEnum#USER}.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private RoleEnum role = RoleEnum.USER;


    /**
     * Набор избранных предметов пользователя.
     * Связь многие-ко-многим с сущностью {@link Item}.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<Item> favoriteItems = new HashSet<>();

    /**
     * Получить коллекцию прав доступа (authorities) пользователя.
     * Используется Spring Security для авторизации.
     *
     * @return список ролей пользователя с префиксом "ROLE_"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
