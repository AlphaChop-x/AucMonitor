//package ru.manakin.aucmonitor.utils;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.kohsuke.github.*;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import ru.manakin.aucmonitor.dto.ItemJson;
//import ru.manakin.aucmonitor.model.CategoryEnum;
//import ru.manakin.aucmonitor.model.Item;
//import ru.manakin.aucmonitor.model.SubcategoryEnum;
//import ru.manakin.aucmonitor.repository.ItemRepository;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class SCimporter implements CommandLineRunner {
//
//    private final ItemRepository itemRepository;
//    private final ObjectMapper objectMapper = new ObjectMapper()
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//    private GitHub gitHub;
//
//    @PostConstruct
//    public void init() throws IOException {
//        this.gitHub = new GitHubBuilder()
//                .withOAuthToken("mysecret")
//                .build();
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("Запуск импорта данных из GitHub...");
//        try {
//            GHRepository repo = gitHub.getRepository("EXBO-Studio/stalcraft-database");
//            log.info("Репозиторий получен: {}", repo.getFullName());
//            importAllItems(repo);
//        } catch (Exception e) {
//            log.error("Ошибка при импорте данных: ", e);
//            throw e;
//        }
//    }
//
//    private void importAllItems(GHRepository repo) throws IOException {
//        try {
//            log.debug("Проверка лимитов GitHub API...");
//            GHRateLimit rateLimit = gitHub.getRateLimit();
//
//            if (rateLimit == null) {
//                log.error("Не удалось получить информацию о лимитах!");
//                return;
//            }
//
//            long resetMillis = rateLimit.getResetDate().getTime() - System.currentTimeMillis();
//            log.info("Лимиты GitHub API: {}/{} запросов, сброс через {} минут",
//                    rateLimit.getRemaining(),
//                    rateLimit.getLimit(),
//                    resetMillis / 60000);
//
//            if (rateLimit.getRemaining() < 100) {
//                String msg = String.format("Мало оставшихся запросов: %d. Рекомендуется подождать.", rateLimit.getRemaining());
//                log.warn(msg);
//                throw new RuntimeException(msg);
//            }
//
//            // Получаем дерево файлов
//            log.debug("Получение дерева файлов...");
//            GHTree tree = repo.getTreeRecursive("main", 1);
//            AtomicInteger processedCount = new AtomicInteger(0);
//            AtomicInteger skippedCount = new AtomicInteger(0);
//
//            log.info("Начало обработки {} файлов...", tree.getTree().size());
//
//            tree.getTree().parallelStream()
//                    .filter(entry -> entry.getPath().startsWith("ru/items/") &&
//                            entry.getPath().endsWith(".json"))
//                    .forEach(entry -> {
//                        try {
//                            GHContent content = repo.getFileContent(entry.getPath());
//                            importItem(content.read(), entry.getPath());
//                            processedCount.incrementAndGet();
//
//                            // Логируем прогресс каждые 100 файлов
//                            if (processedCount.get() % 100 == 0) {
//                                log.info("Обработано {} файлов...", processedCount.get());
//                            }
//                        } catch (Exception e) {
//                            log.warn("Ошибка при обработке файла {}: {}", entry.getPath(), e.getMessage());
//                            skippedCount.incrementAndGet();
//                        }
//                    });
//
//            log.info("Импорт завершен. Успешно: {}, Пропущено: {}",
//                    processedCount.get(), skippedCount.get());
//
//        } catch (IOException e) {
//            log.error("Ошибка в importAllItems: {}", e.getMessage());
//            if (e.getMessage() != null && e.getMessage().contains("API rate limit exceeded")) {
//                log.error("Лимит GitHub API исчерпан!");
//                GHRateLimit rateLimit = gitHub.getRateLimit();
//                if (rateLimit != null) {
//                    long minutesToReset = (rateLimit.getResetDate().getTime() - System.currentTimeMillis()) / 60000;
//                    log.info("Лимит восстановится через {} минут", minutesToReset);
//                }
//            }
//            throw e;
//        }
//    }
//
//    private void importItem(InputStream inputStream, String filePath) {
//        try {
//            ItemJson dto = objectMapper.readValue(inputStream, ItemJson.class);
//            Item entity = map(dto);
//
//            if (!itemRepository.existsByApiId(entity.getApiId())) {
//                log.debug("Импортируем: {} / {} (из {})",
//                        dto.id, entity.getName(), filePath);
//                itemRepository.save(entity);
//            }
//        } catch (Exception e) {
//            log.error("Ошибка при импорте элемента из {}: {}", filePath, e.getMessage());
//            throw new RuntimeException("Failed to import item from " + filePath, e);
//        }
//    }
//
//    private Item map(ItemJson j) {
//        Item it = new Item();
//        it.setApiId(j.id);
//        it.setName(j.name.lines.getOrDefault("ru", j.id));
//
//        String[] parts = j.category.split("/");
//        it.setCategory(CategoryEnum.valueOf(parts[0].toUpperCase()));
//        if (parts.length > 1) {
//            it.setSubCategory(SubcategoryEnum.valueOf(parts[1].toUpperCase()));
//        }
//
//        it.setColor(j.color);
//        it.setWeight(extractWeight(j.infoBlocks));
//        it.setDescription(extractDescription(j.infoBlocks));
//
//        return it;
//    }
//
//    private float extractWeight(List<ItemJson.InfoBlock> blocks) {
//        if (blocks == null) return 0f;
//
//        for (var b : blocks) {
//            if ("numeric".equals(b.type) && b.name != null && b.name.lines != null) {
//                String ru = b.name.lines.getOrDefault("ru", "");
//                if ("Вес".equals(ru) || "Weight".equals(ru)) {
//                    if (b.value != null && b.value.isNumber()) {
//                        return b.value.floatValue();
//                    }
//                }
//            }
//
//            float nested = extractWeight(b.elements);
//            if (nested != 0f) return nested;
//        }
//        return 0f;
//    }
//
//    private String extractDescription(List<ItemJson.InfoBlock> blocks) {
//        if (blocks == null) return null;
//
//        for (var b : blocks) {
//            if ("text".equals(b.type) && b.text != null) {
//                if (b.text.key != null && b.text.key.contains("description")) {
//                    String ru = b.text.lines.get("ru");
//                    if (ru != null && !ru.isBlank()) return ru;
//                }
//            }
//
//            String nested = extractDescription(b.elements);
//            if (nested != null) return nested;
//        }
//        return null;
//    }
//}