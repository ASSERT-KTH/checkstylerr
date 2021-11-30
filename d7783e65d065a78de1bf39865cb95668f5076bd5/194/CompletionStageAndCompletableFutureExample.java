import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Chaklader on 10/27/18.
 */

class TaskSupplier {

    static double getSomeArbitraryDouble() {

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 5;
    }

    static double getAnotherArbitraryDouble() {

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 10;
    }

    static double getValueForCompletableFuture4() {

        LocalDate localDate = LocalDate.now();

        int currDate = localDate.getDayOfMonth();
        if (currDate % 2 == 0)
            return 100;
        else
            return 50;
    }

    static double throwRuntimeException() {
        throw new RuntimeException("Some RuntimeException was thrown");
    }

    static double cancelThisTask() {

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 50D;
    }
}


public class App {


    public static void main(String[] args) {

        /*
         * 
         * ###############################
         * 
         * 1. Why named CompletableFuture?
         * */
        CompletableFuture<Double> completableFuture1 = new CompletableFuture<>();

        new Thread(() -> {

            try {
                Thread.sleep(4000L);
            } catch (Exception e) {
                completableFuture1.complete(-100.0);
            }

            /*
             * we can manually "complete" a CompletableFuture!! this
             * feature is not found with the classical Future interface
             */
            completableFuture1.complete(100.0);
        }, "CompFut1-Thread").start();

        System.out.println("ok...waiting at: " + new Date());
        System.out.format("compFut value and received at: %f, %s \n", completableFuture1.join(), new Date());


        /**
         * 
         * ###########################################################################
         * 
         * 2. chaining multiple CompletionStages dependencies - the "either" construct
         *
         * A CompletionStage may have either/or completion dependency with other 
         * CompletionStages: In the following snippet, completableFutureForAcptEither
         * depends on the completion of either CompletableFuture2 or CompletableFuture3
         */

        //We will create an ExecutorService rather than depending on ForkJoinCommonPool
        ExecutorService exec = Executors.newCachedThreadPool();

        CompletableFuture<Double> completableFuture2
                = CompletableFuture.supplyAsync(TaskSupplier::getSomeArbitraryDouble, exec);

        /*
         * we made TaskSupplier.getSomeArbitraryDouble to delay for 5s to bring asynchrony
         * with task wrapped within CompletableFuture3 (which we would be delaying for 3s)
         * If Operating System does not do schedule these tasks contrary to our expectations,
         * then CompletableFuture3 would complete before completableFuture2.
         */

        CompletableFuture<Double> completableFuture3
                = CompletableFuture.supplyAsync(TaskSupplier::getAnotherArbitraryDouble, exec);

        CompletableFuture<Void> completableFutureForAcptEither
                = completableFuture2.acceptEitherAsync(completableFuture3, (val) -> {
            System.out.println("Value after the completion of completable future : " + val);
        }, exec);


        /*
         * #######################################################################
         * 
         * 3. Chaining multiple CompletableFutures - one-after-the-other construct
         *
         * We can chain various CompletableFutures one after the other provided
         * that the depending CompletableFuture completes normally. The following
         * snippet would clarify the construct. In this example,completableFuture5
         * waits for the completion of completableFuture4, as completableFuture5
         * would execute accordingly depending on the outcome of completableFuture4
         */

        CompletableFuture<Double> completableFuture4
                = CompletableFuture.supplyAsync(TaskSupplier::getValueForCompletableFuture4, exec);

        CompletableFuture<Double> completableFuture5
                = completableFuture4.thenComposeAsync((compFut4) -> {

            if (compFut4 == 100) {
                CompletableFuture<Double> compFut = new CompletableFuture<>();
                compFut.complete(1D);
                return compFut;
            } else if (compFut4 == 50) {
                CompletableFuture<Double> compFutt = new CompletableFuture<>();
                compFutt.complete(0D);
                return compFutt;
            }

            return null;
        }, exec);

        System.out.println("completableFuture 5 : " + completableFuture5.join());



        /*
         * ################################################
         * 
         * 4. CompletableFuture chaining when the depending
         * CompletableFuture completes exceptionally.
         * */
        CompletableFuture<Double> completableFuture6
                = CompletableFuture.supplyAsync(TaskSupplier::throwRuntimeException);

        completableFuture6.exceptionally((throwable) -> {

            if (throwable != null) {
                System.out.println("Exception thrown with message: " + throwable.getMessage());
                return null;
            } else
                return completableFuture6.join();
        });


        /*
         * #############################################
         * 
         * 5. CompletableFuture, if not already complete,
         * can be cancelled with a relevant Exception
         * */
        
        CompletableFuture<Double> completableFuture7
                = CompletableFuture.supplyAsync(TaskSupplier::cancelThisTask);

        boolean isCancelled = completableFuture7.cancel(true);
        System.out.println("Is completableFuture7 cancelled: " + isCancelled);
        System.out.println("Is completableFuture7 completed with exception: " + completableFuture7.isCompletedExceptionally());


        // the completableFuture7 was cancelled and this call will throw a java.util.concurrent.CancellationException  
        System.out.println("Whats the result of task completableFuture7: " + completableFuture7.join());
    }
}


