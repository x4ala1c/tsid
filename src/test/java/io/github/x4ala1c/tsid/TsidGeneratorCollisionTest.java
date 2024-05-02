package io.github.x4ala1c.tsid;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

final class TsidGeneratorCollisionTest {

    private static final int MAX_GENERATORS = 5;

    @AfterEach
    public void resetGenerator() {
        TsidGenerator.reset();
    }

    private static Stream<Arguments> collisionProneGenerator() {
        final List<TsidGenerator> defaultGenerators = new LinkedList<>();
        for (int i = 0; i < MAX_GENERATORS; i++) {
            defaultGenerators.add(TsidGenerator.defaultGenerator());
        }
        final List<TsidGenerator> threadGenerators = new LinkedList<>();
        for (int i = 0; i < MAX_GENERATORS; i++) {
            threadGenerators.add(TsidGenerator.threadGenerator());
        }
        final List<TsidGenerator> customThreadGenerators = new LinkedList<>();
        for (int i = 0; i < MAX_GENERATORS; i++) {
            customThreadGenerators.add(TsidGenerator.threadGenerator(TsidConfiguration.fromEnvironment()));
        }
        final List<TsidGenerator> customGenerators = new LinkedList<>();
        for (int i = 0; i < MAX_GENERATORS; i++) {
            customGenerators.add(TsidGenerator.generator(TsidConfiguration.fromEnvironment()));
        }
        return Stream.<Arguments>builder()
                .add(Arguments.of(defaultGenerators))
                .add(Arguments.of(threadGenerators))
                .add(Arguments.of(customThreadGenerators))
                .add(Arguments.of(customGenerators))
                .build();
    }

    private static final class CollisionTestThread extends Thread {

        private final TsidGenerator generator;
        private final Set<Tsid> resultTsidHolder;
        private final int maxIdsCount;
        private final CountDownLatch countDownLatch;
        private volatile Exception exception;

        private CollisionTestThread(TsidGenerator generator, Set<Tsid> resultTsidHolder, int maxIdsCount,
                                    CountDownLatch countDownLatch) {
            this.generator = generator;
            this.resultTsidHolder = resultTsidHolder;
            this.maxIdsCount = maxIdsCount;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                final List<Tsid> result = new LinkedList<>();
                for (int i = 0; i < maxIdsCount; i++) {
                    result.add(generator.generate());
                }
                resultTsidHolder.addAll(result);
            } catch (Exception e) {
                this.exception = e;
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    @ParameterizedTest
    @MethodSource("collisionProneGenerator")
    void testGeneratorCollision(List<TsidGenerator> generators) throws InterruptedException {
        final int maxTsidCount = 1000;
        final Set<Tsid> resultTsidHolder = ConcurrentHashMap.newKeySet();
        final CountDownLatch countDownLatch = new CountDownLatch(MAX_GENERATORS);
        final List<CollisionTestThread> threads = new LinkedList<>();
        generators.forEach(g -> {
            final CollisionTestThread thread = new CollisionTestThread(g, resultTsidHolder, maxTsidCount, countDownLatch);
            threads.add(thread);
        });
        threads.forEach(Thread::start);
        countDownLatch.await();
        threads.forEach(t -> {
            if (t.exception != null) {
                Assertions.fail(t.exception.getMessage(), t.exception);
            }
        });
        Assertions.assertThat(resultTsidHolder).size().isLessThanOrEqualTo(MAX_GENERATORS * maxTsidCount);
    }

    @Test
    void testGeneratorNoCollision() throws InterruptedException {
        final int maxThreadCount = 10;
        final int maxTsidCount = 1000;
        final Set<Tsid> resultTsidHolder = ConcurrentHashMap.newKeySet();
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreadCount);
        final TsidGenerator generator = TsidGenerator.globalGenerator();
        final List<CollisionTestThread> threads = new LinkedList<>();
        for (int i = 0; i < maxThreadCount; i++) {
            final CollisionTestThread thread = new CollisionTestThread(generator, resultTsidHolder, maxTsidCount, countDownLatch);
            threads.add(thread);
        }
        threads.forEach(Thread::start);
        countDownLatch.await();
        threads.forEach(t -> {
            if (t.exception != null) {
                Assertions.fail(t.exception.getMessage(), t.exception);
            }
        });
        Assertions.assertThat(resultTsidHolder).size().isEqualTo(maxThreadCount * maxTsidCount);
    }

    private static final class GlobalGenerateCollisionTestThread extends Thread {

        private final Set<Tsid> resultTsidHolder;
        private final int maxIdsCount;
        private final CountDownLatch countDownLatch;
        private volatile Exception exception;

        private GlobalGenerateCollisionTestThread(Set<Tsid> resultTsidHolder, int maxIdsCount,
                                                  CountDownLatch countDownLatch) {
            this.resultTsidHolder = resultTsidHolder;
            this.maxIdsCount = maxIdsCount;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                final List<Tsid> result = new LinkedList<>();
                for (int i = 0; i < maxIdsCount; i++) {
                    result.add(TsidGenerator.globalGenerate());
                }
                resultTsidHolder.addAll(result);
            } catch (Exception e) {
                this.exception = e;
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    @Test
    void testGlobalGenerateNoCollision() throws InterruptedException {
        final int maxThreadCount = 10;
        final int maxTsidCount = 1000;
        final Set<Tsid> resultTsidHolder = ConcurrentHashMap.newKeySet();
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreadCount);
        final List<GlobalGenerateCollisionTestThread> threads = new LinkedList<>();
        for (int i = 0; i < maxThreadCount; i++) {
            final GlobalGenerateCollisionTestThread thread = new GlobalGenerateCollisionTestThread(resultTsidHolder,
                    maxTsidCount, countDownLatch);
            threads.add(thread);
        }
        threads.forEach(Thread::start);
        countDownLatch.await();
        threads.forEach(t -> {
            if (t.exception != null) {
                Assertions.fail(t.exception.getMessage(), t.exception);
            }
        });
        Assertions.assertThat(resultTsidHolder).size().isEqualTo(maxThreadCount * maxTsidCount);
    }
}
