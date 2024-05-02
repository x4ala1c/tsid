package io.github.x4ala1c.tsid;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class TsidGeneratorTest {

    @AfterEach
    void resetGlobalGenerator() {
        TsidGenerator.reset();
    }

    @Test
    @SuppressWarnings("all")
    void testCreateGenerator() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            final TsidGenerator generator = TsidGenerator.defaultGenerator();
            Assertions.assertThat(generator).isNotNull();
        });
        Assertions.assertThatNoException().isThrownBy(() -> {
            final TsidGenerator generator = TsidGenerator.threadGenerator();
            Assertions.assertThat(generator).isNotNull();
        });
        Assertions.assertThatNoException().isThrownBy(() -> {
            final TsidGenerator generator = TsidGenerator.threadGenerator(TsidConfiguration.fromEnvironment());
            Assertions.assertThat(generator).isNotNull();
        });
        Assertions.assertThatNullPointerException().isThrownBy(() -> TsidGenerator.threadGenerator(null));
        Assertions.assertThatNullPointerException().isThrownBy(() -> TsidGenerator.generator(null));
    }

    @Test
    void testCreateGlobalGenerator() {
        Assertions.assertThatNoException().isThrownBy(TsidGenerator::globalGenerator);
        TsidGenerator.reset();
        Assertions.assertThatNoException()
                .isThrownBy(() -> TsidGenerator.globalGenerator(TsidConfiguration.fromEnvironment()));
    }

    @Test
    @SetEnvironmentVariable(key = "TSID_NODE", value = "69")
    @SetEnvironmentVariable(key = "TSID_EPOCH", value = "69420")
    void testGenerate() throws ExecutionException, InterruptedException {
        final long baseTimestamp = Instant.now().toEpochMilli();
        final TsidConfiguration configuration = TsidConfiguration.builder()
                .node(96)
                .epoch(96024)
                .build();
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        TsidGenerator generator = TsidGenerator.defaultGenerator();
        scheduledExecutorService.schedule(checkTsidValid(generator, 69, 69420, baseTimestamp),
                100, TimeUnit.MILLISECONDS).get();

        generator = TsidGenerator.generator(configuration);
        scheduledExecutorService.schedule(checkTsidValid(generator, 96, 96024, baseTimestamp),
                100, TimeUnit.MILLISECONDS).get();

        generator = TsidGenerator.threadGenerator();
        scheduledExecutorService.schedule(checkTsidValid(generator, (int) Thread.currentThread().getId(), 69420, baseTimestamp),
                100, TimeUnit.MILLISECONDS).get();

        generator = TsidGenerator.threadGenerator(configuration);
        scheduledExecutorService.schedule(checkTsidValid(generator, (int) Thread.currentThread().getId(), 96024, baseTimestamp),
                100, TimeUnit.MILLISECONDS).get();

        generator = TsidGenerator.globalGenerator();
        scheduledExecutorService.schedule(checkTsidValid(generator, 69, 69420, baseTimestamp),
                100, TimeUnit.MILLISECONDS).get();
        TsidGenerator.reset();

        generator = TsidGenerator.globalGenerator(configuration);
        scheduledExecutorService.schedule(checkTsidValid(generator, 96, 96024, baseTimestamp),
                100, TimeUnit.MILLISECONDS).get();
        TsidGenerator.reset();

        scheduledExecutorService.schedule(() -> {
            final Tsid id = TsidGenerator.globalGenerate();
            Assertions.assertThat(id).isNotNull();
            final long value = id.asLong();
            Assertions.assertThat((value >> 22) + 69420).isGreaterThanOrEqualTo(baseTimestamp);
            Assertions.assertThat((value >> 12) & 0x3FF).isEqualTo(69);
            return null;
        }, 100, TimeUnit.MILLISECONDS).get();
    }

    private Callable<?> checkTsidValid(TsidGenerator generator, int node, long epoch, long baseTimestamp) {
        return () -> {
            final Tsid id = generator.generate();
            Assertions.assertThat(id).isNotNull();
            final long value = id.asLong();
            Assertions.assertThat((value >> 22) + epoch).isGreaterThanOrEqualTo(baseTimestamp);
            Assertions.assertThat((value >> 12) & 0x3FF).isEqualTo(node);
            return null;
        };
    }
}
