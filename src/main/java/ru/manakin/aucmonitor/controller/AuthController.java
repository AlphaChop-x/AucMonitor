package ru.manakin.aucmonitor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.manakin.aucmonitor.model.AppUser;
import ru.manakin.aucmonitor.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Метод возвращающий страничку логина
     *
     * @return {@code login.html}
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Метод, возвращающий страничку регистрации и добавляющий в модель thymeleaf атрибут пользователя
     *
     * @return {@code register.html}
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    /**
     * Метод для проведения регистрации пользователя, проводит валидацию входящих данных, и, если всё хорошо,
     * регистрирует пользователя и возвращает страницу входа
     *
     * @return {@code login.html} или {@code register.html} если возникли ошибки валидации или дубликата ключей
     */
    @PostMapping("/register")
    public String register(
            @Validated @ModelAttribute("user") AppUser user,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("confirmError", true);
            return "register";
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userService.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Имя пользователя уже занято");
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email уже используется");
            return "register";
        }

        userService.registerUser(user);
        model.addAttribute("registered", true);
        return "login";
    }
}
