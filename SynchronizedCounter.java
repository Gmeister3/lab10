public class SynchronizedCounter {
    private int count = 0;

    // Intrinsic lock ensures only one thread executes increment() at a time,
    // eliminating the race condition present in Counter.
    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
