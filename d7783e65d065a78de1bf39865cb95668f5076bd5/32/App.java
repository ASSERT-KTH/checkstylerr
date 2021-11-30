package CountDownLatch_6;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// http://stackoverflow.com/questions/17827022/what-is-countdown-latch-in-java-multithreading
class Processor implements Runnable {

    private CountDownLatch latch;

    public Processor(CountDownLatch latch) {
        this.latch = latch;
    }

    public void run() {

        System.out.println("Started.");

        try {
            Thread.sleep(3000);
        } 

        catch (InterruptedException ignored) {

        }

        latch.countDown();
    }
}




// class for the main application 
public class App {


    public static void main(String[] args) {

        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 3; i++) {
            executor.submit(new Processor(latch));
        }

        executor.shutdown();

        try {
            // Application’s main thread waits, till other service threads which are
            // as an example responsible for starting framework services have completed 
            // started all services.
            latch.await();
        } 

        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Completed.");
    }
}


