package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class MarginTableEntry {

    @JsonProperty(index = 0)
    private Integer id;

    @JsonProperty(index = 1)
    private MarginTableDetail detail;

    // 构造函数、Getter和Setter
    public MarginTableEntry() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MarginTableDetail getDetail() {
        return detail;
    }

    public void setDetail(MarginTableDetail detail) {
        this.detail = detail;
    }
}
