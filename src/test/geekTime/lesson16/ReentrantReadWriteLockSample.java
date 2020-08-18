package test.geekTime.lesson16;


import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 极客时间第16讲例子
 */
public class ReentrantReadWriteLockSample {
    private final Map<String, String> m = new TreeMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    public String get(String key) {
        r.lock();
        System.out.println("读锁锁定！");
        try {
            return m.get(key);
        } finally {
            r.unlock();
        }
    }

    public String put(String key, String entry) {
        w.lock();
        System.out.println("写锁锁定！");
        try {
            return m.put(key, entry);
        } finally {
            w.unlock();
        }
    }
    // …
}
