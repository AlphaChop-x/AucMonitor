package ru.manakin.aucmonitor.repository;

import org.springframework.data.repository.CrudRepository;
import ru.manakin.aucmonitor.model.Item;

public interface ItemRepository extends CrudRepository<Item, Long> {
}
