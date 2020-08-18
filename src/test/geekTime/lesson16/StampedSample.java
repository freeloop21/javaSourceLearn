package test.geekTime.lesson16;


import javax.xml.crypto.Data;
import java.util.concurrent.locks.StampedLock;

/**
 * 极客时间第16讲"synchronized底层如何实现？什么是锁的升级、降级？"例子
 * writeLock 和 unLockWrite
 */
public class StampedSample {
    private final StampedLock sl = new StampedLock();

    void mutate() {
        long stamp = sl.writeLock();
        try {
            write();
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    private void write() {
    }

    Data access() {
        long stamp = sl.tryOptimisticRead();
        Data data = read();
        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                data = read();
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return data;
    }

    private Data read() {
        return null;
    }
    // …
}
