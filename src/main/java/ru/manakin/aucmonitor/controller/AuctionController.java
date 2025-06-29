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
public class AuctionController {

    private final StalcraftApiService stalcraftApiService;

    @RequestMapping("/auction")
    public ResponseEntity<?> getLots(
            @RequestParam String itemId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stalcraftApiService.getAuctionApiResponse(itemId));
    }

}

