package ru.manakin.aucmonitor.repository;

import org.springframework.data.repository.CrudRepository;
import ru.manakin.aucmonitor.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {
    boolean existsAppUserByUsername(String username);

    boolean existsAppUserByEmail(String email);

    Optional<AppUser> findByUsername(String username);
}
