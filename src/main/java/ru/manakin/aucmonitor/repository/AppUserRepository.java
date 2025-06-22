package ru.manakin.aucmonitor.repository;

import org.springframework.data.repository.CrudRepository;
import ru.manakin.aucmonitor.model.AppUser;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {
}
