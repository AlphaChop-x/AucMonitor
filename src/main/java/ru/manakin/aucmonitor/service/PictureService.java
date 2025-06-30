package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.ItemRepository;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class PictureService {
    private final ItemRepository itemRepository;

    public String getPicture(Item item) {
        String baseUrl = "https://raw.githubusercontent.com/EXBO-Studio/stalcraft-database/main/ru/icons/{category}/{subcategory}/{apiId}.png";
        if (item.getSubCategory() == null) {
            baseUrl = baseUrl.replace("{subcategory}/", "");
        } else {
            baseUrl = baseUrl.replace("{subcategory}", item.getSubCategory().toString().toLowerCase());
        }
        baseUrl = baseUrl.replace("{category}", item.getCategory().toString().toLowerCase())
                .replace("{apiId}", item.getApiId());
        System.out.println(baseUrl);
        return baseUrl;
    }
}
