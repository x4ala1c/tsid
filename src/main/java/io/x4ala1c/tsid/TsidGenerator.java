package io.x4ala1c.tsid;

import java.security.SecureRandom;
import java.time.Instant;

public final class TsidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static TsidGenerator instance;

    private final TsidConfiguration configuration;

    private long prevTimestamp;
    private long prevSequence;

    private TsidGenerator(TsidConfiguration configuration) {
        this.configuration = configuration;
    }

    public static TsidGenerator defaultGenerator() {
        return new TsidGenerator(TsidConfiguration.fromEnvironment());
    }

    public static TsidGenerator generator(TsidConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException(TsidErrorMessage.NULL_CONFIGURATION);
        }
        return new TsidGenerator(configuration);
    }

    public static TsidGenerator threadGenerator() {
        final TsidConfiguration tsidConfiguration = TsidConfiguration.builder()
                .node((short) Thread.currentThread().getId())
                .epoch(Instant.EPOCH.toEpochMilli())
                .build();
        return new TsidGenerator(tsidConfiguration);
    }

    public static TsidGenerator threadGenerator(TsidConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException(TsidErrorMessage.NULL_CONFIGURATION);
        }
        final TsidConfiguration noNodeConfiguration = TsidConfiguration.builder()
                .node((short) Thread.currentThread().getId())
                .epoch(configuration.getEpoch())
                .build();
        return new TsidGenerator(noNodeConfiguration);
    }

    public static TsidGenerator globalGenerator() {
        if (instance == null) {
            generateInstance(TsidConfiguration.fromEnvironment());
        }
        return instance;
    }

    public static TsidGenerator globalGenerator(TsidConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException(TsidErrorMessage.NULL_CONFIGURATION);
        }
        if (instance == null) {
            generateInstance(configuration);
        }
        return instance;
    }

    private static synchronized void generateInstance(TsidConfiguration configuration) {
        if (instance == null) {
            instance = new TsidGenerator(configuration);
        }
    }

    public static synchronized void reset() {
        instance = null;
    }

    public static Tsid globalGenerate() {
        if (instance == null) {
            generateInstance(TsidConfiguration.fromEnvironment());
        }
        return instance.generate();
    }

    public synchronized Tsid generate() {
        long currentTimestamp = Instant.now().toEpochMilli();
        if (currentTimestamp > TsidConfiguration.MAX_EPOCH) {
            throw new IllegalStateException("Timestamp exceeded maximum allowed value");
        }
        long currentSequence = this.prevSequence + 1;
        if (currentTimestamp > prevTimestamp) {
            currentSequence = RANDOM.nextInt(TsidConfiguration.MAX_SEQUENCE + 1);
        }
        if (currentSequence > TsidConfiguration.MAX_SEQUENCE) {
            currentSequence = RANDOM.nextInt(TsidConfiguration.MAX_SEQUENCE + 1);
            currentTimestamp++;
        }
        this.prevTimestamp = currentTimestamp;
        this.prevSequence = currentSequence;
        long result = (currentTimestamp - configuration.getEpoch()) << 22;
        result |= ((long) configuration.getNode() << 12);
        result |= currentSequence;
        return Tsid.fromLong(result);
    }
}
