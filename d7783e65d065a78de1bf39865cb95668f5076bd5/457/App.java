
import java.util.StringTokenizer;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;


/*
 * The java.util.concurrent.CyclicBarrier class is a synchronization mechanism that can
 * synchronize threads progressing through some algorithm. In other words, it is a barrier
 * that all threads must wait at, until all threads reach it, before any of the threads
 * can continue.
 *
 * The threads wait for each other by calling the await() method on the CyclicBarrier. Once
 * N threads are waiting at the CyclicBarrier, all threads are released and can continue
 * running.
 * */
public class App {


    /*
     * Thread-0 waiting at barrier 1
     * Thread-1 waiting at barrier 1
     *
     * BarrierAction 1 executed
     *
     * Thread-1 waiting at barrier 2
     * Thread-0 waiting at barrier 2
     *
     * BarrierAction 2 executed
     *
     *
     * Thread-0 done!
     * Thread-1 done!
     *
     * */
    private static class CyclicBarrierRunnable implements Runnable {

        CyclicBarrier barrier1 = null;
        CyclicBarrier barrier2 = null;

        public CyclicBarrierRunnable(CyclicBarrier bar1, CyclicBarrier bar2) {

            this.barrier1 = bar1;
            this.barrier2 = bar2;
        }


        public void run() {

            try {

                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " waiting at barrier 1");
                this.barrier1.await();

                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " waiting at barrier 2");
                this.barrier2.await();

                System.out.println(Thread.currentThread().getName() + " done!");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {


        Runnable barrier1Action = new Runnable() {

            public void run() {
                System.out.println("BarrierAction 1 executed ");
            }
        };

        Runnable barrier2Action = new Runnable() {

            public void run() {
                System.out.println("BarrierAction 2 executed ");
            }
        };


        CyclicBarrier barrier1 = new CyclicBarrier(2, barrier1Action);
        CyclicBarrier barrier2 = new CyclicBarrier(2, barrier2Action);

        CyclicBarrierRunnable b1 = new CyclicBarrierRunnable(barrier1, barrier2);
        CyclicBarrierRunnable b2 = new CyclicBarrierRunnable(barrier1, barrier2);

        new Thread(b1).start();
        new Thread(b2).start();
    }
}
