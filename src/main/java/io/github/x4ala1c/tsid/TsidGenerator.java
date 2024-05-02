package io.github.x4ala1c.tsid;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Generates {@link Tsid} based on the {@link TsidConfiguration}.
 * <p>
 * Users can either get an instance of the generator, or from globally available one per running app.
 * <p>
 * Any methods that does not use {@link TsidConfiguration} will be configured using default {@link TsidConfiguration}.
 */
public final class TsidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static TsidGenerator instance;

    private final TsidConfiguration configuration;

    private long prevTimestamp;
    private long prevSequence;

    private TsidGenerator(TsidConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets default {@link TsidGenerator}.
     *
     * @return Default instance of {@link TsidGenerator}.
     */
    public static TsidGenerator defaultGenerator() {
        return new TsidGenerator(TsidConfiguration.fromEnvironment());
    }

    /**
     * Gets customized {@link TsidGenerator} with {@link TsidConfiguration}.
     *
     * @param configuration The instance of {@link TsidConfiguration}.
     *
     * @return Customized instance of {@link TsidGenerator}.
     *
     * @throws NullPointerException If the {@code configuration} is null.
     */
    public static TsidGenerator generator(TsidConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException(TsidErrorMessage.NULL_CONFIGURATION);
        }
        return new TsidGenerator(configuration);
    }

    /**
     * Gets {@link TsidGenerator} with the node value as the current thread's ID.
     *
     * @return Instance of {@link TsidGenerator}, with the node value as the current thread's ID.
     */
    public static TsidGenerator threadGenerator() {
        final TsidConfiguration tsidConfiguration = TsidConfiguration.builder()
                .node((short) Thread.currentThread().getId())
                .epoch(Instant.EPOCH.toEpochMilli())
                .build();
        return new TsidGenerator(tsidConfiguration);
    }

    /**
     * Gets customized {@link TsidGenerator} with {@link TsidConfiguration} and node value as the current thread's ID.
     *
     * @param configuration The instance of {@link TsidConfiguration}.
     *
     * @return Customized instance of {@link TsidGenerator}, with the node value as the current thread's ID.
     *
     * @throws NullPointerException If the {@code configuration} is null.
     */
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

    /**
     * Gets the globally available {@link TsidGenerator}. By default, this instance uses the default
     * {@link TsidConfiguration}. The implementation uses Singleton pattern.
     * <p>
     * Note that if the instance has already been generated, then recalling this method without {@code reset()} first
     * will return said instantiated instance, and may not reflect the intention of the user.
     *
     * @return Global instance of {@link TsidGenerator}.
     */
    public static TsidGenerator globalGenerator() {
        if (instance == null) {
            generateInstance(TsidConfiguration.fromEnvironment());
        }
        return instance;
    }

    /**
     * Gets the globally available {@link TsidGenerator} with customized {@link TsidConfiguration}. The implementation
     * uses Singleton pattern.
     * <p>
     * Note that if the instance has already been generated, then recalling this method without {@code reset()} first
     * will return said instantiated instance, and may not reflect the intention of the user.
     *
     * @return Global instance of {@link TsidGenerator}.
     */
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

    /**
     * Resets the global {@link TsidGenerator}. Useful when the user wants to change the configuration at runtime.
     */
    public static synchronized void reset() {
        instance = null;
    }

    /**
     * Generates and returns a {@link Tsid} from the global {@link TsidGenerator}. Wrapper call to the
     * {@code generate()}.
     *
     * @return Instance of {@link Tsid}.
     */
    public static Tsid globalGenerate() {
        if (instance == null) {
            generateInstance(TsidConfiguration.fromEnvironment());
        }
        return instance.generate();
    }

    /**
     * Generates and returns a {@link Tsid} from the current {@link TsidGenerator}.
     *
     * @return Instance of {@link Tsid}.
     *
     * @throws IllegalStateException If the current time exceeds the maximum allowable timestamp value.
     */
    public synchronized Tsid generate() {
        long currentTimestamp = Instant.now().toEpochMilli();
        if (currentTimestamp > TsidConfiguration.MAX_EPOCH) {
            throw new IllegalStateException("Timestamp exceeded maximum allowed value");
        }
        long currentSequence = this.prevSequence + 1;
        if (currentTimestamp > prevTimestamp) {
            currentSequence = RANDOM.nextInt(TsidConfiguration.MAX_SEQUENCE + 1);
        } else if (currentTimestamp < prevTimestamp) {
            currentTimestamp = prevTimestamp;
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
