package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.model.AppUser;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.AppUserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final AppUserRepository appUserRepository;

    public boolean existsByEmail(String email) {
        return appUserRepository.existsAppUserByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return appUserRepository.existsAppUserByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public void registerUser(AppUser user) {

        if (appUserRepository.existsAppUserByUsername(user.getUsername())) {
            return;
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        appUserRepository.save(user);
    }
}
