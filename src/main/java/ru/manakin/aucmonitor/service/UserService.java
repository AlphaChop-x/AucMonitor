package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.model.AppUser;
import ru.manakin.aucmonitor.repository.AppUserRepository;

/**
 * Сервис для работы с пользователями приложения.
 * Реализует {@link UserDetailsService} для интеграции с Spring Security.
 * <p>
 * Предоставляет методы проверки существования пользователя по email или username,
 * загрузки данных пользователя для аутентификации и регистрации новых пользователей.
 * При регистрации пароль шифруется с помощью {@link BCryptPasswordEncoder}.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final AppUserRepository appUserRepository;

    /**
     * Простой метод, возвращающий True или False в зависимости от существования в бд пользователя с почтой из параметра
     *
     * @param email ({@link String}) почта пользователя
     * @return {@code Bool(true/false)} ({@link Boolean}) пользователь с такой почтой существует? да/нет
     */
    public boolean existsByEmail(String email) {
        return appUserRepository.existsAppUserByEmail(email);
    }

    /**
     * Простой метод, возвращающий True или False в зависимости от существования в бд пользователя с никнеймом
     * из параметра
     *
     * @param username ({@link String}) никнейм пользователя
     * @return {@code Bool} ({@link Boolean}) пользователь с таким ником существует? да/нет
     */
    public boolean existsByUsername(String username) {
        return appUserRepository.existsAppUserByUsername(username);
    }

    /**
     * Метод необходимый для работы Spring Security, возвращает UserDetails по никнейму пользователя
     *
     * @param username ({@link String}) никнейм пользователя
     * @return {@code UserDetails} ({@link UserDetails}) представление информации о пользователе
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /**
     * Метод необходимый для регистрации пользователя в приложении, принимает AppUser, шифрует пароль и сохраняет в бд
     *
     * @param user ({@link AppUser}) заполненный пользователь с ещё незашифрованным паролем
     * @throws DuplicateKeyException если почта или никнейм уже закреплены за другим пользователем
     */
    public void registerUser(AppUser user) {

        if (appUserRepository.existsAppUserByUsername(user.getUsername())) {
            throw new DuplicateKeyException("Пользователь с таким ником уже существует");
        }

        if (appUserRepository.existsAppUserByEmail(user.getEmail())) {
            throw new DuplicateKeyException("Пользователь с такой почтой уже существует");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        appUserRepository.save(user);
    }
}
