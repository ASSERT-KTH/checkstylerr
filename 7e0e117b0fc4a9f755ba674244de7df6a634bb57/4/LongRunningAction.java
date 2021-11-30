package com.baeldung.concurrent.phaser;

import java.util.concurrent.Phaser;


/*
Java 1.7 
--------

If you need to wait for threads to arrive before you can continue 
or start another set of tasks, then Phaser is a good choice. 

Uses a Phaser to synchronize the tasks in a way that each task in the list needs 
to arrive at the barrier before they are executed in parallel. The task list is 
executed twice. The first cycle is started when both threads arrived at the barrier. 
The second cycle is started when both threads arrived at the barrier.
*/
public class LongRunningAction implements Runnable {
    

    private String threadName;
    private Phaser ph;

    LongRunningAction(String threadName, Phaser ph) {

        this.threadName = threadName;
        this.ph = ph;

        ph.register();
    }

    @Override
    public void run() {

        System.out.println("This is phase " + ph.getPhase());
        System.out.println("Thread " + threadName + " before long running action");

        ph.arriveAndAwaitAdvance();

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ph.arriveAndDeregister();
    }
}