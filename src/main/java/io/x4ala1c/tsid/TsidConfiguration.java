package io.x4ala1c.tsid;

import java.math.BigInteger;
import java.time.Instant;

public final class TsidConfiguration {

    public static final TsidConfiguration DEFAULT = new TsidConfiguration((short) 0, Instant.EPOCH.toEpochMilli());

    static final short MAX_NODE = 1023;
    static final short MAX_SEQUENCE = 4095;
    static final long MAX_EPOCH = BigInteger.valueOf(2).pow(41).longValueExact();

    private final short node;
    private final long epoch;

    private TsidConfiguration(short node, long epoch) {
        this.node = node;
        this.epoch = epoch;
    }

    static final class Builder extends AbstractFluent<Builder> {

        private short node;
        private long epoch;

        private Builder() {
        }

        Builder node(short node) {
            if (node < 0 || node > MAX_NODE) {
                throw new IllegalArgumentException("Node value must be between 0 and " + (MAX_NODE - 1));
            }
            this.node = node;
            return self();
        }

        Builder epoch(long epoch) {
            if (epoch < 0 || epoch > MAX_EPOCH) {
                throw new IllegalArgumentException("Epoch value must be between 0 and " + (MAX_EPOCH - 1));
            }
            this.epoch = epoch;
            return self();
        }

        TsidConfiguration build() {
            return new TsidConfiguration(node, epoch);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    short getNode() {
        return node;
    }

    long getEpoch() {
        return epoch;
    }
}
