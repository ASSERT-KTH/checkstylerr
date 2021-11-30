package com.baeldung.concurrent.runnable;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskRunner {

    private static ExecutorService executorService;

    private static void executeTask() {    
        executorService= Executors.newSingleThreadExecutor();

        EventLoggingTask task = new EventLoggingTask();

        Future future = executorService.submit(task);

        executorService.shutdown();
    }

    public static void main(String[] args) {
        executeTask();
    }
}
