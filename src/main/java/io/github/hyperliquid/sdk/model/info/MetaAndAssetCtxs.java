package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 元数据与资产上下文数组（形如 [meta, assetCtxs]） */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class MetaAndAssetCtxs {
    /** 索引 0：市场元数据 */
    @JsonProperty(index = 0)
    private MetaInfo metaInfo;

    /** 索引 1：各资产上下文列表 */
    @JsonProperty(index = 1)
    private List<AssetCtx> assetCtxs;
}
