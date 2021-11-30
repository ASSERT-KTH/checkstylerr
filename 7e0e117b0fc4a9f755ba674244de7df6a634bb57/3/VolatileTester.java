import java.util.concurrent.*;
import java.util.*;


public class VolatileTester {



    private int a = 0, b = 0;       // change to volatile here
    private int c = 0;

    private final int TEST_COUNT = 100;
    private int[] testResult = new int[TEST_COUNT];


    private static class PhaserRunner implements Runnable {

        private final Phaser phaser;
        private final Runnable runnable;
        private boolean[] enabled;

        public PhaserRunner(Phaser phaser, boolean[] enabled, Runnable runnable) {
            this.phaser = phaser;
            this.runnable = runnable;
            this.enabled = enabled;
        }

        @Override
        public void run() {
            int phase;
            for (;;) {
                phase = phaser.arriveAndAwaitAdvance();
                if (phase < 0) {
                    break;
                } else if (enabled[phase % enabled.length]) {
                    System.out.println("I'm running: " + Thread.currentThread());
                    runnable.run();
                }
            }
        }
    }


    private static void printResult(int[] result) {
        
        final Map<Integer, Integer> countMap = new HashMap<>();
        
        for (final int n : result) {
            countMap.put(n, countMap.getOrDefault(n, 0) + 1);
        }

        countMap.forEach((n, count) -> {        	
            System.out.format("%d -> %d%n", n, count);
        });
    }

    private void runTask1() {
        a = 5;
        b = 10;
    }

    private void runTask2() {

        if (b == 10) {

            if (a == 5) {
                c = 1;
            } 
            else {
                c = 2;
            }
        } 

        else {

            if (a == 5) {
                c = 3;
            } 
            else {
                c = 4;
            }
        }
    }

    private void runTask3() {

        // "reset task"
        a = 0;
        b = 0;
        c = 0;
    }

    public void runTest() throws InterruptedException {
        final Phaser phaser = new Phaser(4);

        final Thread[] threads = new Thread[]{
                // build tree of dependencies here
                new Thread(new PhaserRunner(phaser, new boolean[]{true, false, false}, this::runTask1), "T1"),
                new Thread(new PhaserRunner(phaser, new boolean[]{false, false, true}, this::runTask2), "T2"),
                new Thread(new PhaserRunner(phaser, new boolean[]{false, true, false}, this::runTask3), "T3")
        };

        try {
            for (Thread thread : threads) {
                thread.start();
            }

            for (int i = 0; i < TEST_COUNT; i++) {
                testResult[i] = c;
                phaser.arriveAndAwaitAdvance();
            }
        } finally {
            phaser.forceTermination();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        printResult(testResult);
    }


    public static void main(String[]args) throws Exception {
        new VolatileTester().runTest();
    }
}