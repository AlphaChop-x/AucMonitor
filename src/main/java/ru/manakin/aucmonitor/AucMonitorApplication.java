package ru.manakin.aucmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AucMonitorApplication {
    private static final Logger log = LoggerFactory.getLogger(AucMonitorApplication.class);

    public static void main(String[] args) {
        log.info("=== Тестовое сообщение при запуске ===");  // Добавьте эту строку
        SpringApplication.run(AucMonitorApplication.class, args);
    }
}
