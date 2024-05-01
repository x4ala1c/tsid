package io.x4ala1c.tsid;

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

    @AfterEach
    public void resetGenerator() {
        TsidGenerator.reset();
    }

    private static Stream<Arguments> collisionProneGenerator() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(TsidGenerator.defaultGenerator()))
                .add(Arguments.of(TsidGenerator.threadGenerator()))
                .add(Arguments.of(TsidGenerator.generator(TsidConfiguration.fromEnvironment())))
                .add(Arguments.of(TsidGenerator.threadGenerator(TsidConfiguration.fromEnvironment())))
                .build();
    }

    private static final class CollisionTestThread extends Thread {

        private final TsidGenerator generator;
        private final Set<Tsid> resultTsidHolder;
        private final int maxIdsCount;
        private final CountDownLatch countDownLatch;

        private CollisionTestThread(TsidGenerator generator, Set<Tsid> resultTsidHolder, int maxIdsCount,
                                    CountDownLatch countDownLatch) {
            this.generator = generator;
            this.resultTsidHolder = resultTsidHolder;
            this.maxIdsCount = maxIdsCount;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            final List<Tsid> result = new LinkedList<>();
            for (int i = 0; i < maxIdsCount; i++) {
                result.add(generator.generate());
            }
            resultTsidHolder.addAll(result);
            countDownLatch.countDown();
        }
    }

    @ParameterizedTest
    @MethodSource("collisionProneGenerator")
    void testGeneratorCollision(TsidGenerator generator) throws InterruptedException {
        final int maxThreadCount = 10;
        final int maxTsidCount = 1000;
        final Set<Tsid> resultTsidHolder = ConcurrentHashMap.newKeySet();
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreadCount);
        for (int i = 0; i < maxThreadCount; i++) {
            final Thread thread = new CollisionTestThread(generator, resultTsidHolder, maxTsidCount, countDownLatch);
            thread.start();
        }
        countDownLatch.await();
        Assertions.assertThat(resultTsidHolder).hasSizeLessThan(maxThreadCount * maxTsidCount);
    }

    @Test
    void testGeneratorNoCollision() throws InterruptedException {
        final int maxThreadCount = 10;
        final int maxTsidCount = 1000;
        final Set<Tsid> resultTsidHolder = ConcurrentHashMap.newKeySet();
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreadCount);
        final TsidGenerator generator = TsidGenerator.globalGenerator();
        for (int i = 0; i < maxThreadCount; i++) {
            final Thread thread = new CollisionTestThread(generator, resultTsidHolder, maxTsidCount, countDownLatch);
            thread.start();
        }
        countDownLatch.await();
        Assertions.assertThat(resultTsidHolder).hasSizeLessThan(maxThreadCount * maxTsidCount);
    }

    private static final class GlobalGenerateCollisionTestThread extends Thread {

        private final Set<Tsid> resultTsidHolder;
        private final int maxIdsCount;
        private final CountDownLatch countDownLatch;

        private GlobalGenerateCollisionTestThread(Set<Tsid> resultTsidHolder, int maxIdsCount,
                                                  CountDownLatch countDownLatch) {
            this.resultTsidHolder = resultTsidHolder;
            this.maxIdsCount = maxIdsCount;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            final List<Tsid> result = new LinkedList<>();
            for (int i = 0; i < maxIdsCount; i++) {
                result.add(TsidGenerator.globalGenerate());
            }
            resultTsidHolder.addAll(result);
            countDownLatch.countDown();
        }
    }

    @Test
    void testGlobalGenerateNoCollision() throws InterruptedException {
        final int maxThreadCount = 10;
        final int maxTsidCount = 1000;
        final Set<Tsid> resultTsidHolder = ConcurrentHashMap.newKeySet();
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreadCount);
        for (int i = 0; i < maxThreadCount; i++) {
            final Thread thread = new GlobalGenerateCollisionTestThread(resultTsidHolder, maxTsidCount, countDownLatch);
            thread.start();
        }
        countDownLatch.await();
        Assertions.assertThat(resultTsidHolder).hasSizeLessThan(maxThreadCount * maxTsidCount);
    }
}
