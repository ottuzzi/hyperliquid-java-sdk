package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class MarginTableEntry {

    @JsonProperty(index = 0)
    private Integer id;

    @JsonProperty(index = 1)
    private MarginTableDetail detail;
}
