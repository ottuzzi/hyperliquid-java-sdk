package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class MetaAndAssetCtxs {

    @JsonProperty(index = 0)
    private MetaInfo metaInfo;

    @JsonProperty(index = 1)
    private List<AssetCtx> assetCtxs;

    // 构造函数
    public MetaAndAssetCtxs() {
    }

    public MetaAndAssetCtxs(MetaInfo metaInfo, List<AssetCtx> assetCtxs) {
        this.metaInfo = metaInfo;
        this.assetCtxs = assetCtxs;
    }

    // Getter和Setter
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public List<AssetCtx> getAssetCtxs() {
        return assetCtxs;
    }

    public void setAssetCtxs(List<AssetCtx> assetCtxs) {
        this.assetCtxs = assetCtxs;
    }
}
