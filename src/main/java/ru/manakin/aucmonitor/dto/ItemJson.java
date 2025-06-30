package ru.manakin.aucmonitor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import ru.manakin.aucmonitor.model.ColorEnum;

import java.util.List;
import java.util.Map;

// чтобы Jackson не ругался на «лишнее»
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemJson {
    public String id;
    public String category;            // "weapon/pistol"
    public Name name;                  // вложенный объект
    public ColorEnum color;               // RANK_STALKER, QUALITY_RARE, ...
    public List<InfoBlock> infoBlocks; // для веса

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        public Map<String, String> lines;   // "ru" -> "«Гадюка»"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoBlock {
        public String type;                 // numeric | key-value | text | list …
        public Name name;                   // у numeric-блоков
        public JsonNode value;              // numeric = число, key-value = объект
        public Text text;                   // у text-блоков
        public List<InfoBlock> elements;    // если type == "list"

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Name {
            public Map<String, String> lines;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Text {
            public Map<String, String> lines;  // перевод
//            public String text;               // если строка сразу в JSON
            public String key;
        }
    }


}
