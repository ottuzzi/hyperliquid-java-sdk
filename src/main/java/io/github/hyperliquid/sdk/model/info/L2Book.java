package io.github.hyperliquid.sdk.model.info;

import java.util.List;

public class L2Book {

    private String coin;
    private Long time;
    private List<List<Levels>> levels;

    public static class Levels {

        private String px;
        private String sz;
        private Integer n;

        public String getPx() {
            return px;
        }

        public void setPx(String px) {
            this.px = px;
        }

        public String getSz() {
            return sz;
        }

        public void setSz(String sz) {
            this.sz = sz;
        }

        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        @Override
        public String toString() {
            return "Levels{" +
                    "px='" + px + '\'' +
                    ", sz='" + sz + '\'' +
                    ", n=" + n +
                    '}';
        }
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public List<List<Levels>> getLevels() {
        return levels;
    }

    public void setLevels(List<List<Levels>> levels) {
        this.levels = levels;
    }

    @Override
    public String toString() {
        return "L2Book{" +
                "coin='" + coin + '\'' +
                ", time=" + time +
                ", levels=" + levels +
                '}';
    }
}
