package com.swarm.mobile.dialogs.stamp;

import static com.swarm.mobile.dialogs.stamp.CreateStampDialog.MIN_DEPTH;
import static com.swarm.mobile.dialogs.stamp.CreateStampDialog.SECONDS_PER_DAY;

import com.swarm.mobile.utils.SwarmPostageStampUtils;

import java.math.BigInteger;

public class StampDialogState {

    private int depth = MIN_DEPTH;
    private int ttlValue = 2;
    private long ttlUnit = SECONDS_PER_DAY;
    private int ttlMin = 2;
    public int getDepth() { return depth; }
    public void decrementDepth() { depth--; }
    public void incrementDepth() { depth++; }
    public int getTtlValue() { return ttlValue; }
    public void decrementTtlValue() { ttlValue--; }
    public void incrementTtlValue() { ttlValue++; }
    public void setTtlUnit(long ttlUnit) { this.ttlUnit = ttlUnit; }
    public int getTtlMin() { return ttlMin; }
    public void setTtlMin(int ttlMin) { this.ttlMin = ttlMin; }

    public void clampTtlValue() {
        if (ttlValue < ttlMin) ttlValue = ttlMin;
    }

    public long ttlSeconds() {
        return (long) ttlValue * ttlUnit;
    }

    public long computeAmount(BigInteger currentPricePerBlock) {
        return SwarmPostageStampUtils.calculateAmountFromTTL(
                ttlSeconds(), currentPricePerBlock.longValue());
    }
}
