package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.manakin.aucmonitor.dto.ApiHistoryDto;
import ru.manakin.aucmonitor.dto.ApiLotsDto;
import ru.manakin.aucmonitor.dto.LotDto;
import ru.manakin.aucmonitor.dto.HistoryDto;

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

    /**
     * Метод для получения url предмета, для запроса к апи сталкрафта
     *
     * @param url    ({@code https://eapi.stalcraft.net/{region}/auction/{item}/lots}) private final переменная класса
     * @param itemId ({@code String}) переменная соответствующая id предмета в апи сталкрафта
     * @return url ({@link String}) возвращает url, по которому можно совершать запрос
     */
    private String getApiUrl(String url, String itemId) {
        return url.replace("{region}", "ru").replace("{item}", itemId);
    }

    /**
     * Метод возвращающий необходимые для авторизации запроса к апи хедеры
     *
     * @return clientId и clientSecret ({@link HttpHeaders}) 2 хедера айди и сикрет
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Client-Secret", clientSecret);
        return headers;
    }

    /**
     * Метод создающий request и получающий response от сталкрафт апи сервера
     *
     * @param itemId айди предмета для работы со сталкрафт апи
     * @param sort   параметр сортировки, закрепляется как query param, позволяет получить сразу отсортированный список
     *               лотов, хотя api сортировка работает не очень хорошо, приходится сортировать повторно
     * @param order  параметр сортировки, по возрастанию, или по убыванию
     * @return response ({@link ApiLotsDto}) dto состоящее из двух частей ({@code ApiLotsDto.total} и
     * {@code ApiLotsDto.lots}) total - обычная строка-число, а lots - лист, из ({@link LotDto}):
     * @throws IllegalArgumentException если метод получает id = null
     */
    public ApiLotsDto getAuctionApiResponse(String itemId, String sort, String order) {

        if (itemId == null) {
            throw new IllegalArgumentException("Item id cannot be null");
        }

        String url = getApiUrl(apiUrl, itemId);

        //При первой загрузке страницы эти параметры будут равны 0, что бы не ломать запрос, добавляю
        //наиболее предпочтительные варианты
        sort = sort == null ? "buyout_price" : sort;
        order = order == null ? "asc" : order;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("order", order)
                .queryParam("additional", List.of("true"))
                .queryParam("limit", 100);

        if (!Objects.equals(sort, "priceForOne")) {
            builder.queryParam("sort", sort);
        } else {
            builder.queryParam("sort", "buyout_price");
        }

        HttpEntity<String> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiLotsDto> response = null;
        try {
            response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    request,
                    ApiLotsDto.class
            );
        } catch (HttpClientErrorException e) {
            ApiLotsDto errorResponse = new ApiLotsDto();
            errorResponse.setTotal("0");
            errorResponse.setLots(Collections.emptyList());
            errorResponse.setErrorMessage("Слишком много запросов, попробуйте обновить страницу чуть погодя");
            return errorResponse;
        }

        fillAucPosition(response.getBody());

        //Апи не предоставляют информацию о цене товара за 1 штуку, так что приходится самому считать, можно было
        //выделить в отдельный метод, но расчёт занимает 1 строчку кода, так что не вижу проблем
        for (LotDto lot : response.getBody().lots) {
            lot.setPriceForOne(lot.getBuyoutPrice() / Integer.parseInt(lot.getAmount()));
        }

        log.info("Найдено лотов: {}", response.getBody().total);

//        //Также апи любит присылать рофло несуществующие лоты с ценой выкупа 0, такие отсеиваем
//        response.getBody().lots.removeIf(lot -> lot.buyoutPrice == 0);

        //Применяем метод для сортировки, потому что опять же апи даже с параметром сортировки, возвращает список с
        //неточностями, обязательно нужно перепроверять
        return generalSort(response.getBody(), sort, order);
    }


    /**
     * Метод для выбора и применения сортировки к response
     *
     * @param lots {@link ApiLotsDto} список лотов
     * @param sort порядок сортировки
     * @return {@link ApiLotsDto} отсортированный список лотов
     */
    private ApiLotsDto generalSort(ApiLotsDto lots, String sort, String order) {
        switch (sort) {
            case "priceForOne":
                lots = sortLotsByOnePiecePrice(lots, order);
                break;
            case "buyout_price":
                lots = sortLotsByBuyoutPrice(lots, order);
                break;
            case "time_left":
                lots = sortLotsByRemainingTime(lots, order);
                break;
            default:
                break;
        }
        adaptTimeFormat(lots);
        return lots;
    }

    /**
     * Метод для заполнения позиций лотов для более быстрого поиска, в расчёт берётся, что лоты отсортированы по цене
     * выкупа
     *
     * @param lots {@link ApiLotsDto} список лотов
     */
    private void fillAucPosition(ApiLotsDto lots) {
        int i = 1;
        for (LotDto lot : lots.lots) {
            int page = (i - 1) / 50 + 1;
            int position = (i - 1) % 50 + 1;
            lot.setPosition(String.format("Страница: %d, позиция %d", page, position));
            i++;
        }
    }

    /**
     * Метод создающий request и получающий response от сталкрафт апи сервера
     *
     * @param itemId айди предмета для работы со сталкрафт апи
     * @param limit  параметр, указывающий сколько вернуть записей из истории, стандартно - 20, максимум - 200
     * @return response ({@link ApiHistoryDto}) dto состоящее из двух частей ({@code ApiHistoryDto.total} и
     * {@code ApiHistoryDto.prices}) total - обычная строка-число, а prices - лист, из ({@link HistoryDto}):
     * @throws IllegalArgumentException если метод получает id = null
     */
    public ApiHistoryDto getPriceHistoryResponse(String itemId, String limit) {

        if (itemId == null) {
            throw new IllegalArgumentException("Item id cannot be null");
        }

        String url = getApiUrl(historyUrl, itemId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("limit", limit);

        HttpEntity<String> request = new HttpEntity<>(getHeaders());

        ResponseEntity<ApiHistoryDto> response = restTemplate.exchange(
                builder.build().toUri(),
                HttpMethod.GET,
                request,
                ApiHistoryDto.class
        );

        log.info("Найдено записей: {}", response.getBody().total);

        return response.getBody();
    }


    /**
     * Метод для конвертации времени в более понятный для восприятия формат yyyy-MM-dd HH:mm:ss
     *
     * @param apiLotsDto ({@link  ApiLotsDto}) дто, содержащее 2 переменные: кол-во лотов, и, собственно, сами лоты
     */
    private void adaptTimeFormat(ApiLotsDto apiLotsDto) {

        apiLotsDto.setLots(apiLotsDto.lots.stream()
                .filter(Objects::nonNull)
                .peek(lot -> {
                    LocalDateTime dateTime = LocalDateTime.parse(lot.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    lot.setEndTime(dateTime.format(formatter));
                })
                .collect(Collectors.toList()));
    }

    /**
     * Метод для сортировки лотов в ApiLotsDto по цене за штуку
     *
     * @param apiLotsDto ({@link ApiLotsDto}) апи дто, содержащее количество лотов и список всех лотов
     * @return {@code apiLotsDto} ({@link ApiLotsDto}) дто с уже отсортированными лотами
     */
    public ApiLotsDto sortLotsByOnePiecePrice(ApiLotsDto apiLotsDto, String order) {

        Comparator<LotDto> comparator = "desc".equals(order)
                ? Comparator.comparingDouble(LotDto::getPriceForOne).reversed()
                : Comparator.comparingDouble(LotDto::getPriceForOne);

        List<LotDto> sortedLots = apiLotsDto.lots.stream()
                .sorted(comparator)
                .toList();

        apiLotsDto.setLots(sortedLots);
        return apiLotsDto;
    }

    /**
     * Метод для сортировки лотов в ApiLotsDto по цене выкупа
     *
     * @param apiLotsDto ({@link ApiLotsDto}) апи дто, содержащее количество лотов и список всех лотов
     * @return {@code apiLotsDto} ({@link ApiLotsDto}) дто с уже отсортированными лотами
     */
    public ApiLotsDto sortLotsByBuyoutPrice(ApiLotsDto apiLotsDto, String order) {

        Comparator<LotDto> comparator = "desc".equals(order)
                ? Comparator.comparingDouble(LotDto::getBuyoutPrice).reversed()
                : Comparator.comparingDouble(LotDto::getBuyoutPrice);

        List<LotDto> sortedLots = apiLotsDto.lots.stream()
                .sorted(comparator)
                .toList();

        apiLotsDto.setLots(sortedLots);
        return apiLotsDto;
    }

    /**
     * Метод для сортировки лотов в ApiLotsDto по оставшемуся времени
     *
     * @param apiLotsDto ({@link ApiLotsDto}) апи дто, содержащее количество лотов и список всех лотов
     * @return {@code apiLotsDto} ({@link ApiLotsDto}) дто с уже отсортированными лотами
     * @throws IllegalArgumentException выбрасывается если метод получает null переменную
     *                                  или если в ней пустой {@code apiLotsDro.lots}
     */
    public ApiLotsDto sortLotsByRemainingTime(ApiLotsDto apiLotsDto, String order) {


        Instant now = Instant.now();

        Comparator<LotDto> comparator = Comparator.comparingLong(LotDto -> {
            Instant endTime = Instant.parse(LotDto.getEndTime());
            return Duration.between(now, endTime).toMillis();
        });

        if (order.equals("desc")) {
            comparator = comparator.reversed();
        }

        List<LotDto> sortedLots = apiLotsDto.lots.stream()
                .sorted(comparator)
                .toList();

        apiLotsDto.setLots(sortedLots);
        return apiLotsDto;
    }
}
