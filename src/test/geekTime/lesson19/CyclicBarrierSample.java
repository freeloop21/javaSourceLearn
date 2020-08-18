package test.geekTime.lesson19;


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 我们知道 CyclicBarrier 其实反映的是线程并行运行时的协调，在下面的示例里，从逻辑上，
 * 5 个工作线程其实更像是代表了 5 个可以就绪的空车，而不再是 5 个乘客，对比前面 CountDownLatch 的例子更有助于我们区别它们的抽象模型
 */

// CountDownLatch: 一个线程(或者多个)， 等待另外N个线程完成某个事情之后才能执行。（有一个countDown()方法，await方法会一直等待直至CountDownLatch的值被countDown为0）   等待事件
// CyclicBrrier: N个线程相互等待，任何一个线程完成之前，所有的线程都必须等待。 （只有一个await方法，所有线程调用await直至CyclicBarrier的值变为0）         等待线程
public class CyclicBarrierSample {
    public static void main(String[] args) throws InterruptedException {
        //使用了 CyclicBarrier 特有的 barrierAction，当屏障被触发时，Java 会自动调度该动作
        CyclicBarrier barrier = new CyclicBarrier(5, new Runnable() {
            @Override
            public void run() {
                System.out.println("Action...GO again!");
            }
        });
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(new CyclicWorker(barrier));
            t.start();
        }
    }
    static class CyclicWorker implements Runnable {
        private CyclicBarrier barrier;
        public CyclicWorker(CyclicBarrier barrier) {
            this.barrier = barrier;
        }
        @Override
        public void run() {
            try {
                for (int i=0; i<3 ; i++){
                    System.out.println(Thread.currentThread().getName()+"  第" + (i+1) + "次循环  start");
                    // 此时第一次循环开始值为5，此时调用barrier.await()减1进行等待；
                    // 等5个线程都启动之后，此时barrier值变为0，barrier达到屏障；
                    // 此时5个线程都汇集到此处，所有的线程得到了释放，进而执行下面的逻辑；
                    // 达到屏障后，barrier会重置为5
                    barrier.await();
                    System.out.println(Thread.currentThread().getName()+"  第" + (i+1) + "次循环  end");
                }
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}