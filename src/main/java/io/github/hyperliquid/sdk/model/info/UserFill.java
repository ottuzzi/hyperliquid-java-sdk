package io.github.hyperliquid.sdk.model.info;

/**
 * Retrieve a user's fills
 * 用户最近成交
 **/
public class UserFill {

    private String coin;
    private String px;
    private String sz;
    private String side;
    private Long time;
    private String startPosition;
    private String dir;
    private String closedPnl;
    private String hash;
    private Long oid;
    private Boolean crossed;
    private String fee;
    private Long tid;
    private String feeToken;
    private String twapId;
    private String builderFee;


    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

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

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(String startPosition) {
        this.startPosition = startPosition;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getClosedPnl() {
        return closedPnl;
    }

    public void setClosedPnl(String closedPnl) {
        this.closedPnl = closedPnl;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public Boolean getCrossed() {
        return crossed;
    }

    public void setCrossed(Boolean crossed) {
        this.crossed = crossed;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getFeeToken() {
        return feeToken;
    }

    public void setFeeToken(String feeToken) {
        this.feeToken = feeToken;
    }

    public String getTwapId() {
        return twapId;
    }

    public void setTwapId(String twapId) {
        this.twapId = twapId;
    }

    public String getBuilderFee() {
        return builderFee;
    }

    public void setBuilderFee(String builderFee) {
        this.builderFee = builderFee;
    }

    // 实用方法 - 判断是否为现货交易
    public boolean isSpotTrade() {
        return coin != null && coin.startsWith("@");
    }

    // 实用方法 - 判断是否为永续合约交易
    public boolean isPerpTrade() {
        return coin != null && !coin.startsWith("@");
    }

    // 实用方法 - 获取资产ID（如果是现货交易）
    public String getAssetId() {
        if (isSpotTrade() && coin != null) {
            return coin.substring(1); // 去掉"@"符号
        }
        return coin;
    }

    @Override
    public String toString() {
        return "UserFill{" +
                "coin='" + coin + '\'' +
                ", px='" + px + '\'' +
                ", sz='" + sz + '\'' +
                ", side='" + side + '\'' +
                ", time=" + time +
                ", startPosition='" + startPosition + '\'' +
                ", dir='" + dir + '\'' +
                ", closedPnl='" + closedPnl + '\'' +
                ", hash='" + hash + '\'' +
                ", oid=" + oid +
                ", crossed=" + crossed +
                ", fee='" + fee + '\'' +
                ", tid=" + tid +
                ", feeToken='" + feeToken + '\'' +
                ", twapId='" + twapId + '\'' +
                ", builderFee='" + builderFee + '\'' +
                '}';
    }
}
