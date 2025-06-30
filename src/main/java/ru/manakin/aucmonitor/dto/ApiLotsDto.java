package ru.manakin.aucmonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.List;

@Setter
public class ApiLotsDto {
    @JsonProperty("total")
    public String total;

    @JsonProperty("lots")
    public List<LotDto> lots;
}
