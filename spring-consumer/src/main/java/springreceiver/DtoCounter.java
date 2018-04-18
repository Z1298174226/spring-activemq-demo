package springreceiver;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class DtoCounter {
    private final String key;

    private final AtomicLong count = new AtomicLong(0);
    private final long first = System.currentTimeMillis();
    private long last = System.currentTimeMillis();

    public DtoCounter(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DtoCounter that = (DtoCounter) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Key '" + key + "' processed " + count.get() + " in " + (last - first) + "ms.";
    }

    public void increment() {
        this.count.incrementAndGet();
        last = System.currentTimeMillis();
    }
}
