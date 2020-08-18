package test.geekTime.lesson19;

import java.util.concurrent.Semaphore;


/**
 * 场景
 * 在车站、机场等出租车时，当很多空出租车就位时，为防止过度拥挤，调度员指挥排队等待坐车的队伍一次进来 5 个人上车，
 * 等这 5 个人坐车出发，再放进去下一批，这和 Semaphore 的工作原理有些类似。
 */
public class UsualSemaphoreSample {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Action...GO!");
        Semaphore semaphore = new Semaphore(5);
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new SemaphoreWorker(semaphore));
            t.start();
        }
    }
}
class SemaphoreWorker implements Runnable {
    private String name;
    private Semaphore semaphore;
    public SemaphoreWorker(Semaphore semaphore) {
        this.semaphore = semaphore;
    }
    @Override
    public void run() {
        try {
            log("is waiting for a permit!");
            semaphore.acquire();
            log("acquired a permit!");
            log("executed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            log("released a permit!");
            semaphore.release();
        }
    }
    private void log(String msg){
        if (name == null) {
            name = Thread.currentThread().getName();
        }
        System.out.println(name + " " + msg);
    }
}

/**
 * 从具体节奏来看，其实并不符合我们前面场景的需求，因为本例中 Semaphore 的用法实际是保证，一直有 5 个人可以试图乘车，
 * 如果有 1 个人出发了，立即就有排队的人获得许可，而这并不完全符合我们前面的要求。
 */
class AbnormalSemaphoreSample {
    public static void main(String[] args) throws InterruptedException {
        //permits可以初始化为0，release后会增加
        Semaphore semaphore = new Semaphore(0);
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new MyWorker(semaphore));
            t.start();
        }
        System.out.println("Action...GO!");
        semaphore.release(5);
        System.out.println("Wait for permits off");
        while (semaphore.availablePermits()!=0) {
            Thread.sleep(1000L);
        }
        System.out.println("Action...GO again!");
        semaphore.release(5);
    }
}
class MyWorker implements Runnable {
    private Semaphore semaphore;
    public MyWorker(Semaphore semaphore) {
        this.semaphore = semaphore;
    }
    @Override
    public void run() {
        try {
            semaphore.acquire();
            System.out.println("Executed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
