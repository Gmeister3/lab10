import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main test driver demonstrating race conditions, synchronization, and the
 * Executor framework as required by Lab 10.
 *
 * Concurrency Analysis
 * --------------------
 * The unsynchronized Counter uses a non-atomic read-modify-write sequence
 * (count++). In a multithreaded environment multiple threads can read the same
 * stale value before any of them writes back, causing lost updates. This
 * nondeterministic interleaving means the final count is almost always less
 * than the expected 20,000 – a classic race condition.
 *
 * SynchronizedCounter wraps increment() in an intrinsic (monitor) lock. Only
 * one thread may hold the lock at a time, so each read-modify-write is
 * atomic with respect to other threads. The lock is simple and correct but
 * introduces contention: every thread must wait its turn.
 *
 * AtomicCounter uses AtomicInteger.incrementAndGet(), which relies on
 * Compare-And-Swap (CAS) CPU instructions. CAS is lock-free: a thread retries
 * the operation only when another thread concurrently changed the value. Under
 * high contention AtomicInteger can outperform synchronized because it avoids
 * full mutual exclusion.
 *
 * The FixedThreadPool (ExecutorService) decouples task submission from thread
 * lifecycle management. Rather than creating and destroying a new Thread for
 * every one of the 20,000 tasks – which is expensive – a pool of reusable
 * worker threads picks up tasks from a shared queue. This dramatically reduces
 * thread-creation overhead and caps resource consumption.
 *
 * executor.shutdown() signals that no new tasks will be submitted, and
 * awaitTermination() blocks until all queued tasks finish (or a timeout
 * elapses), ensuring the final count is read only after every increment has
 * completed.
 */
public class Main {

    private static final int TOTAL_INCREMENTS = 20_000;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) throws InterruptedException {
        demonstrateRaceCondition();
        demonstrateSynchronizedCounter();
        demonstrateAtomicCounter();
    }

    // -----------------------------------------------------------------------
    // Task 2 – Manual threads, unsynchronized Counter (race condition)
    // -----------------------------------------------------------------------
    private static void demonstrateRaceCondition() throws InterruptedException {
        System.out.println("=== Race Condition Demo (unsynchronized Counter) ===");
        Counter counter = new Counter();

        Thread[] threads = new Thread[TOTAL_INCREMENTS];
        for (int i = 0; i < TOTAL_INCREMENTS; i++) {
            threads[i] = new Thread(counter::increment);
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Expected : " + TOTAL_INCREMENTS);
        System.out.println("Actual   : " + counter.getCount());
        System.out.println("(Actual may be less than expected due to race condition)");
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Task 3 & 4 – FixedThreadPool with SynchronizedCounter
    // -----------------------------------------------------------------------
    private static void demonstrateSynchronizedCounter() throws InterruptedException {
        System.out.println("=== SynchronizedCounter with FixedThreadPool ===");
        SynchronizedCounter counter = new SynchronizedCounter();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        for (int i = 0; i < TOTAL_INCREMENTS; i++) {
            executor.submit(counter::increment);
        }

        // Task 5 – shutdown and awaitTermination
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            System.err.println("Warning: executor timed out before all tasks completed.");
            executor.shutdownNow();
        }

        System.out.println("Expected : " + TOTAL_INCREMENTS);
        System.out.println("Actual   : " + counter.getCount());
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Task 3 & 4 – FixedThreadPool with AtomicCounter
    // -----------------------------------------------------------------------
    private static void demonstrateAtomicCounter() throws InterruptedException {
        System.out.println("=== AtomicCounter with FixedThreadPool ===");
        AtomicCounter counter = new AtomicCounter();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        for (int i = 0; i < TOTAL_INCREMENTS; i++) {
            executor.submit(counter::increment);
        }

        // Task 5 – shutdown and awaitTermination
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            System.err.println("Warning: executor timed out before all tasks completed.");
            executor.shutdownNow();
        }

        System.out.println("Expected : " + TOTAL_INCREMENTS);
        System.out.println("Actual   : " + counter.getCount());
        System.out.println();
    }
}
