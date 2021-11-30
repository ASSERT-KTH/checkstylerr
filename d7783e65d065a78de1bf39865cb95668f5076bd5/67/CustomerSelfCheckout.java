package Multithreading.CustomerSelfCheckout; /**
 * Created by Chaklader on 1/14/17.
 */

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CustomerSelfCheckout {

    private final static long SCALE = 100L;

    static class Customer implements Callable<Void> {

        private final long timeToWait;
        private final CountDownLatch exitLatch;

        Customer(int timeToWait, CountDownLatch exitLatch) {
            this.timeToWait = timeToWait * SCALE;
            this.exitLatch = exitLatch;
        }

        public Void call() throws Exception {
            Thread.sleep(this.timeToWait);
            this.exitLatch.countDown();
            return null;
        }
    }


    public static int solveSuperMarketQueue(int[] customers, int n){

        try {

            ExecutorService service = Executors.newFixedThreadPool(n);
            CountDownLatch exitLatch = new CountDownLatch(n);

            Queue<Customer> queue = new LinkedList<Customer>();

            for (int i : customers) {
                queue.add(new Customer(i, exitLatch));
            }

            long startTime = System.currentTimeMillis();
            service.invokeAll(queue);
            exitLatch.await();
            long wholeTime = System.currentTimeMillis() - startTime;
            service.shutdown();

            return (int) (wholeTime / SCALE + (wholeTime % SCALE == 0 ? 0 : 1));
        }

        catch(InterruptedException ex){
            System.out.println("some error");
        }

        return -1;
    }

    // testAllMethods the customer self-checkout system
    // --------------------------------------
    public static void systemTest() {

        System.out.println("hello");
        int[] arr = {12,3};
        int n = 5;

        System.out.println(solveSuperMarketQueue(arr, n));
    }
}

