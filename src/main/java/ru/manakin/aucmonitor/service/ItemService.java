package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item findItemByApiId(String apiId) {
        return itemRepository.findByApiId(apiId).orElse(null);
    }

    public List<Item> findByNameIgnoreCase(String search) {
        return itemRepository.findByNameContainingIgnoreCase(search);
    }

    public List<Item> findAll() {
        return (List<Item>) itemRepository.findAll();
    }
}
