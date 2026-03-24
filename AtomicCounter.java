import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter {
    // AtomicInteger uses CAS (compare-and-swap) hardware instructions,
    // providing thread-safe increments without explicit locking.
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}
