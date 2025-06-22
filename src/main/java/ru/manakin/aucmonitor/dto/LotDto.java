package ru.manakin.aucmonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class LotDto {
    @JsonProperty("itemId")
    private String itemId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("startPrice")
    private long startPrice;

    @JsonProperty("currentPrice")
    private long currentPrice;

    @JsonProperty("buyoutPrice")
    private long buyoutPrice;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("endTime")
    private String endTime;

    @JsonProperty("additional")
    private JsonNode additional;
}
