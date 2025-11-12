package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** 保证金表详情（描述与各保证金层级） */
@Data
public class MarginTableDetail {
    
    /** 描述信息 */
    @JsonProperty("description")
    private String description;

    /** 保证金层级列表 */
    @JsonProperty("marginTiers")
    private List<MarginTier> marginTiers;

    /** 无参构造函数 */
    public MarginTableDetail() {
    }
}
