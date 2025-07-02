package ru.manakin.aucmonitor.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.controller.exceptions.ItemNotFoundException;
import ru.manakin.aucmonitor.controller.exceptions.NotFoundUserException;
import ru.manakin.aucmonitor.model.AppUser;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.AppUserRepository;
import ru.manakin.aucmonitor.repository.ItemRepository;

import java.util.Set;


/**
 * Сервис для управления списком избранных предметов пользователя.
 * Обеспечивает добавление, удаление и получение предметов,
 * которые пользователь пометил как избранные.
 * <p>
 * Работа происходит с учётом аутентифицированного пользователя,
 * данные которого получаются из Spring Security {@link Authentication}.
 * </p>
 * <p>
 * Использует репозитории для доступа к данным пользователей и предметов,
 * а также {@link jakarta.persistence.EntityManager} для обновления состояния сущностей.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final ItemRepository itemRepository;
    private final AppUserRepository appUserRepository;
    private final EntityManager entityManager;


    /**
     * Метод, добавляющий переданный первым параметром предмет в список избранного
     * аутентифицированного пользователя
     *
     * @param item           ({@link Item}) объект предмета.
     * @param authentication ({@link Authentication}) данные аутентифицированного пользователя
     * @throws NotFoundUserException    если пользователь не найден в бд
     * @throws IllegalArgumentException если хотя бы один из заданных параметров null
     */
    public void addItemToFavorites(
            Item item, Authentication authentication
    ) {

        if (item == null || authentication == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        AppUser appUser = (AppUser) authentication.getPrincipal();

        AppUser managedUser = appUserRepository.findById(appUser.getId())
                .orElseThrow(() -> new NotFoundUserException("User not found"));

        if (!managedUser.getFavoriteItems().contains(item)) {
            managedUser.getFavoriteItems().add(item);
            appUserRepository.save(managedUser);
        }
    }

    /**
     * Метод, удаляющий переданный первым параметром предмет из списка избранного
     * аутентифицированного пользователя
     *
     * @param item           ({@link Item}) объект предмета.
     * @param authentication ({@link Authentication}) данные аутентифицированного пользователя
     * @throws NotFoundUserException    если пользователь не найден в бд
     * @throws IllegalArgumentException если хотя бы один из заданных параметров null
     * @throws ItemNotFoundException    если предмет не найден в бд
     */
    @Transactional
    public void deleteItemFromFavorites(
            Item item, Authentication authentication
    ) {

        if (item == null || authentication == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        AppUser appUser = (AppUser) authentication.getPrincipal();

        AppUser managedUser = appUserRepository.findById(appUser.getId())
                .orElseThrow(() -> new NotFoundUserException("User not found"));

        Item managedItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new ItemNotFoundException("Item with id: " + item.getId() + " not found"));

        if (managedUser.getFavoriteItems().contains(managedItem)) {
            managedUser.getFavoriteItems().remove(managedItem);
            appUserRepository.saveAndFlush(managedUser);
        }

        entityManager.refresh(managedUser);
    }

    /**
     * Метод, возвращающий список избранных аутентифицированным пользователем предметов
     *
     * @param authentication ({@link Authentication}) данные аутентифицированного пользователя
     * @return {@code Set<Item>} ({@link Item}) набор избранных предметов пользователя
     * @throws NotFoundUserException если пользователь не найден в бд
     */
    public Set<Item> getFavoriteItems(Authentication authentication) {

        AppUser appUser = (AppUser) authentication.getPrincipal();

        AppUser managedUser = appUserRepository.findById(appUser.getId())
                .orElseThrow(() -> new NotFoundUserException("User not found"));

        return managedUser.getFavoriteItems();
    }
}
