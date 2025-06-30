package ru.manakin.aucmonitor.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import ru.manakin.aucmonitor.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends CrudRepository<Item, Long> {
    List<Item> findByNameContainingIgnoreCase(String search);

    Optional<Item> findByApiId(String apiId);
}
