package ru.manakin.aucmonitor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import ru.manakin.aucmonitor.model.ColorEnum;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemJson {
    public String id;
    public String category;
    public Name name;
    public ColorEnum color;
    public List<InfoBlock> infoBlocks;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        public Map<String, String> lines;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoBlock {
        public String type;
        public Name name;
        public JsonNode value;
        public Text text;
        public List<InfoBlock> elements;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Name {
            public Map<String, String> lines;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Text {
            public Map<String, String> lines;
            public String key;
        }
    }


}
