package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.manakin.aucmonitor.model.Item;

@Service
@Slf4j
@RequiredArgsConstructor
public class PictureService {

    UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString("https://raw.githubusercontent.com")
            .pathSegment("EXBO-Studio", "stalcraft-database", "main", "ru", "icons");

    /**
     * Метод принимающий объект предмета и возвращающий URL для подгрузки картинки с GitHub репозитория Stalcraft DB
     *
     * @param item ({@link Item}) переданный предмет, для которого необходимо загрузить картинку
     * @return {@code baseUrl} ({@link String}) модифицированная ссылка, ведущая к соответствующей предмету картинке
     * @throws IllegalArgumentException если вместо предмета, категории предмета или apiId предмета передан null
     */
    public String getPicture(
            Item item
    ) {

        if (item == null) {
            throw new IllegalArgumentException("Item is null");
        }

        if (item.getCategory() == null || item.getApiId() == null) {
            throw new IllegalArgumentException("Item category or apiId is null");
        }

        UriComponentsBuilder localBuilder = builder.cloneBuilder();

        localBuilder.pathSegment(item.getCategory().toString().toLowerCase());

        if (item.getSubCategory() != null) {
            localBuilder.pathSegment(item.getSubCategory().toString().toLowerCase());
        }

        localBuilder.pathSegment(item.getApiId() + ".png");

        String url = localBuilder.build().toUriString();

        log.debug("item icon url: {}", url);

        return url;
    }
}
