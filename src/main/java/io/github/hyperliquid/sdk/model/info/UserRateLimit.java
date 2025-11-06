package io.github.hyperliquid.sdk.model.info;

/**
 * Query user rate limits
 **/
public class UserRateLimit {

    public String cumVlm;
    public Long nRequestsUsed;
    public Long nRequestsCap;

    public String getCumVlm() {
        return cumVlm;
    }

    public void setCumVlm(String cumVlm) {
        this.cumVlm = cumVlm;
    }

    public Long getnRequestsUsed() {
        return nRequestsUsed;
    }

    public void setnRequestsUsed(Long nRequestsUsed) {
        this.nRequestsUsed = nRequestsUsed;
    }

    public Long getnRequestsCap() {
        return nRequestsCap;
    }

    public void setnRequestsCap(Long nRequestsCap) {
        this.nRequestsCap = nRequestsCap;
    }

    @Override
    public String toString() {
        return "UserRateLimit{" +
                "cumVlm='" + cumVlm + '\'' +
                ", nRequestsUsed=" + nRequestsUsed +
                ", nRequestsCap=" + nRequestsCap +
                '}';
    }
}
