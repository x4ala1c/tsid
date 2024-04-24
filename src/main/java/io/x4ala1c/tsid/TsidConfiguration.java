package io.x4ala1c.tsid;

import java.math.BigInteger;
import java.time.Instant;

public final class TsidConfiguration {

    static final int MAX_NODE = 1023;
    static final int MAX_SEQUENCE = 4095;
    static final long MAX_EPOCH = BigInteger.valueOf(2).pow(41).longValueExact();

    private final int node;
    private final long epoch;

    TsidConfiguration(int node, long epoch) {
        if (node < 0 || node > MAX_NODE) {
            throw new IllegalArgumentException("Node value must be between 0 and " + MAX_NODE);
        }
        if (epoch < 0 || epoch > MAX_EPOCH) {
            throw new IllegalArgumentException("Epoch value must be between 0 and " + MAX_EPOCH);
        }
        this.node = node;
        this.epoch = epoch;
    }

    static TsidConfiguration fromEnvironment() {
        String nodeFromEnv = System.getProperty("tsid.node");
        if (nodeFromEnv == null) {
            nodeFromEnv = System.getenv("TSID_NODE");
        }
        short node = (short) (Thread.currentThread().getId() % (MAX_NODE + 1));
        if (nodeFromEnv != null) {
            node = Short.parseShort(nodeFromEnv);
        }
        String epochFromEnv = System.getProperty("tsid.epoch");
        if (epochFromEnv == null) {
            epochFromEnv = System.getenv("TSID_EPOCH");
        }
        long epoch = Instant.EPOCH.toEpochMilli();
        if (epochFromEnv != null) {
            epoch = Long.parseLong(epochFromEnv);
        }
        return new TsidConfiguration(node, epoch);
    }

    static final class Builder {

        private int node;
        private long epoch;

        private Builder() {
        }

        Builder node(int node) {
            this.node = node;
            return this;
        }

        Builder epoch(long epoch) {
            this.epoch = epoch;
            return this;
        }

        TsidConfiguration build() {
            return new TsidConfiguration(node, epoch);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    int getNode() {
        return node;
    }

    long getEpoch() {
        return epoch;
    }
}
