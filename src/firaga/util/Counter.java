package firaga.util;

public class Counter {

    private final int maxCount;
    private int count;

    public Counter(int mc) {
        maxCount = mc;
        count = 0;
    }

    public synchronized int getNext() {
        final int current = count;
        count++;
        if (count == maxCount)
            count = 0;
        return current;
    }

}
