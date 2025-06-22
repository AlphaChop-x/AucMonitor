package ru.manakin.aucmonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApiResponseDto {
    @JsonProperty("total")
    private String total;

    @JsonProperty("lots")
    private List<LotDto> lots;
}
