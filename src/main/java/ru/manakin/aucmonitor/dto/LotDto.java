package ru.manakin.aucmonitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LotDto {
    @JsonProperty("itemId")
    private String itemId;

    @JsonProperty("amount")
    public String amount;

    @JsonProperty("startPrice")
    public long startPrice;

    @JsonProperty("currentPrice")
    public long currentPrice;

    @JsonProperty("buyoutPrice")
    public long buyoutPrice;

    public String position;

    public long priceForOne;

    @JsonProperty("startTime")
    public String startTime;

    @JsonProperty("endTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd HH:mm:ss")
    public String endTime;

    @JsonProperty("additional")
    public JsonNode additional;
}
