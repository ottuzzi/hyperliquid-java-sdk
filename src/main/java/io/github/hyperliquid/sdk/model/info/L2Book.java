package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

import java.util.List;

@Data
public class L2Book {

    private String coin;
    private Long time;
    private List<List<Levels>> levels;

    @Data
    public static class Levels {
        private String px;
        private String sz;
        private Integer n;
    }
}
