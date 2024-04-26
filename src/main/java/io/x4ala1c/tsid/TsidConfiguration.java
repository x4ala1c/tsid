package io.x4ala1c.tsid;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Contains configuration information for {@link TsidGenerator} to work with.
 * <p>
 * Currently, the configuration has 2 parameters:
 * <ul>
 * <li> {@code node}: Represents the current node's or machine's ID (when in multi-node system). This is to
 *      ensure that each node will have its generator be uniquely defined when compare to other nodes, reducing the
 *      chance of collision. By default, it is set to the current thread's ID that this {@link TsidConfiguration} is
 *      running in. Acceptable range is [0, 1023].
 * <li> {@code epoch}: Represents the epoch to calculate the timestamp with. By default, {@link TsidConfiguration}
 *      uses Unix epoch. Acceptable range is [0, 4095].
 * </ul>
 * <p>
 * Note that the acceptable ranges are calculated from the integer standpoint. Refer to {@link Tsid} to check the
 * constraints in bits.
 * <p>
 * Users can customize above parameters through either the environment or system variables. The keys for these
 * parameters in each case are:
 * <ul>
 * <li> {@code node}:
 *      <ul>
 *          <li> {@code TSID_NODE} for the environment.
 *          <li> {@code tsid.node} for the system.
 *      </ul>
 * <li> {@code epoch}:
 *      <ul>
 *          <li> {@code TSID_EPOCH} for the environment.
 *          <li> {@code tsid.epoch} for the system.
 *      </ul>
 * </ul>
 * <p>
 * Users can also customize the {@link TsidConfiguration} on code-level using the provided {@link Builder}.
 * <p>
 * For ease of use, and also for any unforeseen expansion of the configuration, create a new {@link TsidConfiguration}
 * through constructor is prohibited, and {@link Builder} is required to create a new instance of
 * {@link TsidConfiguration}.
 */
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

    /**
     * Gets the default {@link TsidConfiguration} from the environment, first, and then from default values, if the
     * environment does not have those configurations.
     *
     * @return Default {@link TsidConfiguration}.
     */
    static TsidConfiguration fromEnvironment() {
        String nodeFromEnv = System.getProperty("tsid.node");
        if (nodeFromEnv == null) {
            nodeFromEnv = System.getenv("TSID_NODE");
        }
        int node = (short) (Thread.currentThread().getId() % (MAX_NODE + 1));
        if (nodeFromEnv != null) {
            node = Integer.parseInt(nodeFromEnv);
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

    /**
     * Simple Builder pattern for {@link TsidConfiguration}.
     */
    public static final class Builder {

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

    /**
     * Gets a new {@link Builder} for {@link TsidConfiguration}.
     *
     * @return new instance of {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the {@code node} value.
     *
     * @return {@code node} value.
     */
    int getNode() {
        return node;
    }

    /**
     * Gets the {@code epoch} value.
     *
     * @return {@code epoch} value.
     */
    long getEpoch() {
        return epoch;
    }
}
