package firaga.util;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private final int maxCount;
    private AtomicInteger count;

    public Counter(int maxCount) {
        this.maxCount = maxCount;
        this.count.set(0);
    }

    public int getNext() {
        return this.count.updateAndGet(n -> (n+1 == maxCount) ? 0 : n+1);
    }

}
