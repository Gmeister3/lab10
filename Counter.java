public class Counter {
    private int count = 0;
    // Non-atomic increment operation
    public void increment() {
        count++; 
    }
    public int getCount() {
        return count;
    }
}