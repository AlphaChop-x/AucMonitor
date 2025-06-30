package ru.manakin.aucmonitor.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiHistoryDto {
    public int total;
    public List<HistoryDto> prices;
}
