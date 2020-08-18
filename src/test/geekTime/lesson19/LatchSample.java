package test.geekTime.lesson19;


import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch 的调度方式相对简单，后一批次的线程进行 await，等待前一批 countDown 足够多次。
 * 这个例子也从侧面体现出了它的局限性，虽然它也能够支持 10 个人排队的情况，但是因为不能重用，如果要支持更多人排队，就不能依赖一个 CountDownLatch 进行了。
 */

// CountDownLatch: 一个线程(或者多个)， 等待另外N个线程完成某个事情之后才能执行。（有一个countDown()方法，await方法会一直等待直至CountDownLatch的值被countDown为0）  等待事件
// CyclicBrrier: N个线程相互等待，任何一个线程完成之前，所有的线程都必须等待。 （只有一个await方法，所有线程调用await直至CyclicBarrier的值变为0）         等待线程
public class LatchSample {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(6);
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(new FirstBatchWorker(latch));
            t.start();
        }
        for (int i = 0; i < 5; i++) {
        Thread t = new Thread(new SecondBatchWorker(latch));
        t.start();
    }
    // 注意这里也是演示目的的逻辑，并不是推荐的协调方式。通常不建议使用这个循环等待方式。
        while ( latch.getCount() != 1 ){
        Thread.sleep(2000L);
    }
        System.out.println("Wait for first batch finish");
        latch.countDown();
    }
}
class FirstBatchWorker implements Runnable {
    private CountDownLatch latch;
    public FirstBatchWorker(CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void run() {
        System.out.println("First batch executed!");
        latch.countDown();//
    }
}
class SecondBatchWorker implements Runnable {
    private CountDownLatch latch;
    public SecondBatchWorker(CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void run() {
        try {
            latch.await();//一直等待知道CountDownLatch的值变为0才执行后面的逻辑
            System.out.println("Second batch executed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

