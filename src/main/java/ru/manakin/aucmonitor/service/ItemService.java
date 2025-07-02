package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.ItemRepository;

import java.util.List;


/**
 * Сервис для работы с сущностями {@link Item}.
 * Обеспечивает поиск предметов по идентификатору API,
 * поиск по имени с игнорированием регистра,
 * а также получение всех предметов.
 * <p>
 * Использует {@link ItemRepository} для доступа к данным.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    /**
     * Поиск предмета по уникальному идентификатору, используемому во внешнем API.
     *
     * @param apiId идентификатор предмета в API
     * @return объект {@link Item} если найден, иначе {@code null}
     */
    public Item findItemByApiId(String apiId) {
        return itemRepository.findByApiId(apiId).orElse(null);
    }

    /**
     * Поиск предметов, название которых содержит указанную строку,
     * без учёта регистра символов.
     *
     * @param search строка для поиска в названии предметов
     * @return список найденных предметов {@link Item}
     */
    public List<Item> findByNameIgnoreCase(String search) {
        return itemRepository.findByNameContainingIgnoreCase(search);
    }

    /**
     * Получение списка всех предметов из базы данных.
     *
     * @return список всех предметов {@link Item}
     */
    public List<Item> findAll() {
        return (List<Item>) itemRepository.findAll();
    }
}