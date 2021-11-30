import static java.lang.String.*;

import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class PhaserUsage implements Callable<String> {

    private static final int THREAD_POOL_SIZE = 10;
    private final Phaser phaser;

    private PhaserUsage(Phaser phaser) {
        this.phaser = phaser;
    }

    public static void main(String a[]) {
        ExecutorService execService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CompletionService<String> completionService = new ExecutorCompletionService<>(execService);

        // since we know beforehand how many tasks we have, initialize the
        // number of participants in the constructor; other wise register
        // *before* launching the task
        Phaser phaser = new Phaser(THREAD_POOL_SIZE);

        IntStream.range(0, THREAD_POOL_SIZE)
                .forEach(nbr -> completionService.submit(new PhaserUsage(phaser)));

        execService.shutdown();

         try {
             while (!execService.isTerminated()) {
                String result = completionService.take().get();
                System.out.println(format("Result is: %s", result));
             }
          } catch (ExecutionException | InterruptedException e) {
             e.printStackTrace();
          }
    }

    @Override
    public String call() {
        String threadName = Thread.currentThread().getName();
        System.out.println(format("Arrive and await advance...%s",threadName));
        phaser.arriveAndAwaitAdvance(); // await all creation
        int a = 0, b = 1;
        Random random = new Random();
        for (int i = 0; i < random.nextInt(10000000); i++) {
            a = a + b;
            b = a - b;
        }
        System.out.println(format("De-registering...%s",threadName));
        phaser.arriveAndDeregister();
        return format("Thread %s results: a = %s, b = %s", threadName, a, b);
    }
}