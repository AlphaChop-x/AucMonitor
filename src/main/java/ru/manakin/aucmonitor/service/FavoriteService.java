package ru.manakin.aucmonitor.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.model.AppUser;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.AppUserRepository;
import ru.manakin.aucmonitor.repository.ItemRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final ItemRepository itemRepository;
    private final AppUserRepository appUserRepository;
    private final EntityManager entityManager;

    public void addItemToFavorites(
            Item item, Authentication authentication
    ) {
        AppUser appUser = (AppUser) authentication.getPrincipal();

        AppUser managedUser = appUserRepository.findById(appUser.getId()).orElseThrow();

        managedUser.getFavoriteItems().add(item);

        appUserRepository.save(managedUser);
    }

    @Transactional
    public void deleteItemFromFavorites(Item item, Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        AppUser managedUser = appUserRepository.findById(user.getId()).orElseThrow();
        Item managedItem = itemRepository.findById(item.getId()).orElseThrow();

        managedUser.getFavoriteItems().remove(managedItem);

        appUserRepository.saveAndFlush(managedUser);

        entityManager.refresh(managedUser);
    }

    public Set<Item> getFavoriteItems(Authentication authentication) {

        AppUser appUser = (AppUser) authentication.getPrincipal();

        AppUser managedUser = appUserRepository.findById(appUser.getId()).orElseThrow();

        return managedUser.getFavoriteItems();
    }
}
