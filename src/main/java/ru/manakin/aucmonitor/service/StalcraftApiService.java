package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.manakin.aucmonitor.dto.ApiHistoryDto;
import ru.manakin.aucmonitor.dto.ApiLotsDto;
import ru.manakin.aucmonitor.dto.LotDto;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StalcraftApiService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://eapi.stalcraft.net/{region}/auction/{item}/lots";
    private final String historyUrl = "https://eapi.stalcraft.net/{region}/auction/{item}/history";
    @Value("${stalcraft.api.clientId}")
    private String clientId;
    @Value("${stalcraft.api.clientSecret}")
    private String clientSecret;

    private String getApiUrl(String url, String itemId) {
        return url.replace("{region}", "ru").replace("{item}", itemId);
    }

    public ApiLotsDto getAuctionApiResponse(String itemId, String sort, String order) {
        String url = getApiUrl(apiUrl, itemId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Client-Secret", clientSecret);
//        headers.setAccept(Collections.singletonList(MediaType.ALL));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("additional", List.of("true"));
        if (!Objects.equals(sort, "priceForOne")) {
            params.put("sort", Collections.singletonList(sort));
        }
        params.put("order", Collections.singletonList(order));

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParams(params);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ApiLotsDto> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                request,
                ApiLotsDto.class
        );

        log.info("Найдено лотов: {}", response.getBody().total);

        response.getBody().lots.removeIf(lot -> {
            lot.setPriceForOne(lot.buyoutPrice / Integer.parseInt(lot.amount));
            return lot.buyoutPrice == 0;
        });

        return response.getBody();
    }

    public ApiHistoryDto getPriceHistoryResponse(String itemId, String count) {
        String url = getApiUrl(historyUrl, itemId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Client-Secret", clientSecret);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("limit", Collections.singletonList(count));

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParams(params);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ApiHistoryDto> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                request,
                ApiHistoryDto.class
        );

        log.info("Найдено записей: {}", response.getBody().total);

        return response.getBody();
    }

    public ApiLotsDto adaptTimeFormat(ApiLotsDto apiLotsDto) {
        apiLotsDto.setLots(apiLotsDto.lots.stream()
                .filter(Objects::nonNull)
                .peek(lot -> {
                    LocalDateTime dateTime = LocalDateTime.parse(lot.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    lot.setEndTime(dateTime.format(formatter));
                })
                .collect(Collectors.toList()));
        return apiLotsDto;
    }

    public ApiLotsDto sortLotsByOnePiecePrice(ApiLotsDto apiLotsDto) {
        List<LotDto> sortedLots = apiLotsDto.lots.stream()
                .sorted(Comparator.comparingDouble(LotDto::getPriceForOne))
                .toList();

        apiLotsDto.setLots(sortedLots);
        return apiLotsDto;
    }

    public ApiLotsDto sortLotsByBuyoutPrice(ApiLotsDto apiLotsDto) {
        List<LotDto> sortedLots = apiLotsDto.lots.stream()
                .sorted(Comparator.comparingLong(LotDto::getBuyoutPrice))
                .toList();

        apiLotsDto.setLots(sortedLots);
        return apiLotsDto;
    }

    public ApiLotsDto sortLotsByRemainingTime(ApiLotsDto apiLotsDto) {
        Instant now = Instant.now(); // Текущее время в UTC

        List<LotDto> sortedLots = apiLotsDto.lots.stream()
                .sorted(Comparator.comparingLong(lot -> {
                    Instant endTime = Instant.parse(lot.getEndTime());
                    return Duration.between(now, endTime).toMillis(); // Сортировка по миллисекундам
                }))
                .toList();

        apiLotsDto.setLots(sortedLots);
        return apiLotsDto;
    }

    private static String formatDurationSimple(Duration duration) {
        long totalSeconds = duration.getSeconds();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
