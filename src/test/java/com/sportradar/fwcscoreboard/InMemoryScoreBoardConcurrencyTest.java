package com.sportradar.fwcscoreboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportradar.fwcscoreboard.model.Match;
import com.sportradar.fwcscoreboard.model.MatchId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * {@link InMemoryScoreBoard} documents itself as safe for concurrent use. These tests exercise that
 * claim under real contention rather than taking it on faith: many threads hammering the board at
 * once, asserting the board never ends up in a state a sequential caller couldn't have produced.
 */
class InMemoryScoreBoardConcurrencyTest {

    private static final int THREAD_COUNT = 16;
    private static final int MATCHES_PER_THREAD = 50;

    @Test
    void startingManyMatchesConcurrentlyProducesNoDuplicatesAndNoLostMatches()
            throws InterruptedException {
        InMemoryScoreBoard board = new InMemoryScoreBoard();
        Set<MatchId> observedIds = ConcurrentHashMap.newKeySet();

        runConcurrently(THREAD_COUNT, threadIndex -> {
            for (int i = 0; i < MATCHES_PER_THREAD; i++) {
                String home = "Home-" + threadIndex + "-" + i;
                String away = "Away-" + threadIndex + "-" + i;
                MatchId id = board.startMatch(home, away);
                // A duplicate here would mean two threads raced onto the same sequence value.
                assertThat(observedIds.add(id)).isTrue();
            }
        });

        assertThat(observedIds).hasSize(THREAD_COUNT * MATCHES_PER_THREAD);
        assertThat(board.getSummary()).hasSize(THREAD_COUNT * MATCHES_PER_THREAD);
    }

    @Test
    void concurrentScoreUpdatesOnDistinctMatchesAreAllApplied() throws InterruptedException {
        InMemoryScoreBoard board = new InMemoryScoreBoard();
        List<MatchId> ids = IntStream.range(0, THREAD_COUNT)
                .mapToObj(i -> board.startMatch("Home-" + i, "Away-" + i))
                .toList();

        runConcurrently(THREAD_COUNT, threadIndex -> {
            MatchId id = ids.get(threadIndex);
            for (int goal = 1; goal <= MATCHES_PER_THREAD; goal++) {
                board.updateScore(id, goal, 0);
            }
        });

        for (MatchId id : ids) {
            Match match = board.getMatch(id).orElseThrow();
            assertThat(match.score().home()).isEqualTo(MATCHES_PER_THREAD);
        }
    }

    @Test
    void onlyOneStartSucceedsWhenTwoThreadsRaceToStartTheSameTeam() throws InterruptedException {
        InMemoryScoreBoard board = new InMemoryScoreBoard();
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejections = new AtomicInteger();

        runConcurrently(THREAD_COUNT, threadIndex -> {
            try {
                board.startMatch("Mexico", "Opponent-" + threadIndex);
                successes.incrementAndGet();
            } catch (RuntimeException e) {
                rejections.incrementAndGet();
            }
        });

        // Mexico can only be in one in-progress match; exactly one racer should have won.
        assertThat(successes.get()).isEqualTo(1);
        assertThat(rejections.get()).isEqualTo(THREAD_COUNT - 1);
        assertThat(board.getSummary()).hasSize(1);
    }

    private void runConcurrently(int threadCount, java.util.function.IntConsumer task)
            throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            int threadIndex = t;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.accept(threadIndex);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // release all threads at once to maximise contention
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();
        assertThat(completed).as("all threads finished within the timeout").isTrue();
    }
}
