package com.sample;

public class MyRunnable implements Runnable {

    NotThreadSafe instance = null;

    public MyRunnable(NotThreadSafe instance) {
        this.instance = instance;
    }

    public void run() {
        this.instance.add("some text");
    }
}