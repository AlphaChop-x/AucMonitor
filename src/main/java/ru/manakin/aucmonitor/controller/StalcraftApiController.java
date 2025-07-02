package ru.manakin.aucmonitor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manakin.aucmonitor.service.StalcraftApiService;

@RestController
@RequiredArgsConstructor
public class StalcraftApiController {

    private final StalcraftApiService stalcraftApiService;

    /**
     * Метод, необходимый для взаимодействия с сервисом апи сталкрафта, возвращает список лотов
     *
     * @param itemId    id предмета заданное разработчиком для работы с api
     * @param sortBy    порядок сортировки, в котором будут возвращены лоты
     * @param direction порядок возрастания/убывания
     * @return {@link ResponseEntity<ru.manakin.aucmonitor.dto.ApiLotsDto>}
     * содержащий {@link ru.manakin.aucmonitor.dto.ApiLotsDto} и {@code HttpStatus.OK}
     */
    @RequestMapping("/auction")
    public ResponseEntity<?> getLots(
            @RequestParam String itemId,
            @RequestParam String sortBy,
            @RequestParam String direction

    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stalcraftApiService.getAuctionApiResponse(itemId, sortBy, direction));
    }

}

