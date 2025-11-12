package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

import java.util.List;

/** L2 订单簿快照（前 10 档买卖盘） */
@Data
public class L2Book {

    /** 币种名称（如 "BTC"） */
    private String coin;
    /** 快照时间戳（毫秒） */
    private Long time;
    /** 买卖盘列表：索引 0 为买盘，索引 1 为卖盘 */
    private List<List<Levels>> levels;

    @Data
    public static class Levels {
        /** 该档位价格（字符串） */
        private String px;
        /** 该档位挂单总数量（字符串） */
        private String sz;
        /** 该价位的挂单笔数/档位计数 */
        private Integer n;
    }
}
