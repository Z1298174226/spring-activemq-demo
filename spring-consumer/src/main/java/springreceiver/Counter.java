package springreceiver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Counter {
    private ConcurrentMap<String, DtoCounter> values = new ConcurrentHashMap<>();

    public void updateCounter(String key) {
        values.putIfAbsent(key, new DtoCounter(key));
        final DtoCounter counter = values.get(key);
        counter.increment();
    }

    public DtoCounter get(String key) {
        return values.get(key);
    }
}
