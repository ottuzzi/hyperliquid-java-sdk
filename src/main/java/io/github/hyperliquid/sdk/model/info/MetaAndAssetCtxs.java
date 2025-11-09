package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class MetaAndAssetCtxs {
    @JsonProperty(index = 0)
    private MetaInfo metaInfo;

    @JsonProperty(index = 1)
    private List<AssetCtx> assetCtxs;
}