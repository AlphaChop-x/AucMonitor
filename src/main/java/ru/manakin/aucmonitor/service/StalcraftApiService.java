package ru.manakin.aucmonitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.manakin.aucmonitor.dto.ApiResponseDto;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StalcraftApiService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://eapi.stalcraft.net/{region}/auction/{item}/lots";
    @Value("${stalcraft.api.clientId}")
    private String clientId;
    @Value("${stalcraft.api.clientSecret}")
    private String clientSecret;

    private String getApiUrl(String itemId) {
        return apiUrl.replace("{region}", "ru").replace("{item}", itemId);
    }

    public ApiResponseDto getAuctionApiResponse(String itemId) {
        String url = getApiUrl(itemId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Client-Secret", clientSecret);
        headers.setAccept(Collections.singletonList(MediaType.ALL));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("additional", List.of("true"));

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParams(params);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ApiResponseDto> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                request,
                ApiResponseDto.class,
                params
        );

        return response.getBody();
    }

}
