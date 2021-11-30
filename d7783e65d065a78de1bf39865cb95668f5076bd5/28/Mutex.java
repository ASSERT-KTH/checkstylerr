package com.multi;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;


public class Mutex {

    
    static Object cLock = new Object();
    static LinkedList<String> cList = new LinkedList<String>();

    // Semaphore maintains a set of permits.
    // Each acquire blocks if necessary until a permit is available, and then takes it.
    // Each release adds a permit, potentially releasing a blocking acquirer.
    static Semaphore semaphore = new Semaphore(0);
    static Semaphore mutex = new Semaphore(1);

    // I'll producing new Integer every time
    static class CrunchifyProducer extends Thread {
        public void run() {

            int counter = 1;
            try {
                while (true) {
                    String threadName = Thread.currentThread().getName() + counter++;

                    mutex.acquire();
                    cList.add(threadName);
                    System.out.println("Producer is prdoucing new value: " + threadName);
                    mutex.release();

                    // release lock
                    semaphore.release();
                    Thread.sleep(500);
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }


    // I'll be consuming Integer every stime
    static class CrunchifyConsumer extends Thread {
        
        String consumerName;

        public CrunchifyConsumer(String name) {
            this.consumerName = name;
        }

        public void run() {

            try {

                while (true) {

                    // acquire lock. Acquires the given number of permits from this semaphore, blocking until all are
                    // available
                    // process stops here until producer releases the lock
                    semaphore.acquire();

                    // Acquires a permit from this semaphore, blocking until one is available
                    mutex.acquire();
                    String result = "";
                    for (String value : cList) {
                        result = value + ",";
                    }
                    System.out.println(consumerName + " consumes value: " + result + "cList.size(): "
                            + cList.size() + "\n");
                    mutex.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new CrunchifyProducer().start();
        new CrunchifyConsumer("Crunchify").start();
        new CrunchifyConsumer("Google").start();
        new CrunchifyConsumer("Yahoo").start();
    }
}
