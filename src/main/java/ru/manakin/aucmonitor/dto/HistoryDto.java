package ru.manakin.aucmonitor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryDto {
    public String time;
    public int price;
    public int amount;
}
