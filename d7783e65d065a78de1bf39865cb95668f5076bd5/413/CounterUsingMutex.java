package com.baeldung.concurrent.semaphores;

import java.util.concurrent.Semaphore;

public class CounterUsingMutex {

    private final Semaphore mutex;
    private int count;

    CounterUsingMutex() {
        mutex = new Semaphore(1);
        count = 0;
    }

    public void increase() throws InterruptedException {
        mutex.acquire();
        this.count = this.count + 1;
        Thread.sleep(1000);
        mutex.release();
    }

    public int getCount() {
        return this.count;
    }


    /*
        "hasQueuedThreads" method queries whether any threads are waiting to 
        acquire. Note that because cancellations may occur at any time, a true 
        return does not guarantee that any other thread will ever acquire. 
    */
    public  boolean hasQueuedThreads() {
        return mutex.hasQueuedThreads();
    }
}
