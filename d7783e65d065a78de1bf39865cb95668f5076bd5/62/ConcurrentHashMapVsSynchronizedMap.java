package Multithreading.ConcurrentHashMapVsSynchronizedMap;

/**
 * Created by Chaklader on 1/15/17.
 */


/*
            Assignment
            ----------
a. Create object for each HashTable, SynchronizedMap and ConcurrentHashMap
b. Add and retrieve 500k entries from Map
c. Measure start and end time and display time in milliseconds
d. Use ExecutorService to run 5 threads in parallel */


/*How a HashMap can be Synchronized in Java and the comparison
among HashMap Vs. ConcurrentHashMap Vs. SynchronizedMap*/


/* HashMap is a non-synchronized collection class.

Questions
---------
What’s the difference between ConcurrentHashMap and Collections.synchronizedMap(Map)?
What’s the difference between ConcurrentHashMap and Collections.synchronizedMap(Map) in term of performance?
ConcurrentHashMap vs Collections.synchronizedMap()


The Map object is an associative containers that store elements, formed by a combination of a uniquely
identify key and a mapped value. If you have very highly concurrent application in which you may want
to modify or read key value in different threads then it’s ideal to use Concurrent Hashmap. Best example
is Producer Consumer which handles concurrent read/write.

What does the thread-safe Map means?

If multiple threads access a hash map concurrently, and at least one of the threads modifies the map
structurally, it must be synchronized externally to avoid an inconsistent view of the contents.

How to make a thread-safe Map ?

There are two ways we could synchronized HashMap - Java Collections synchronizedMap() method
and Use ConcurrentHashMap

========================================================================================================================
Compare the differences among HashMap Vs. synchronizedMap Vs. ConcurrentHashMap ?

a. //HashMap
Map<String, String> normalMap = new HashMap<String, String>();

b. //synchronizedMap
synchronizedHashMap = Collections.synchronizedMap(new HashMap<String, String>());

Synchronization at Object level. Every read/write operation needs to acquire lock. Locking the entire collection is
a performance overhead. This essentially gives access to only one thread to the entire map & blocks all the other
threads. It may cause contention. SynchronizedHashMap returns Iterator, which fails-fast on concurrent modification.


c. //ConcurrentHashMap
concurrentHashMap = new ConcurrentHashMap<String, String>();

You should use ConcurrentHashMap when you need very high concurrency in your project. It is thread safe without
synchronizing the whole map. Reads can happen very fast while write is done with a lock. There is no locking at
the object level. The locking is at a much finer granularity at a hashmap bucket level. ConcurrentHashMap doesn’t
throw a ConcurrentModificationException if one thread tries to modify it while another is iterating over it.
ConcurrentHashMap uses multitude of locks.

======================================================================================================================== */

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ConcurrentHashMapVsSynchronizedMap {

    public final static int THREAD_POOL_SIZE = 5;
    public static Map<String, Integer> hashTableObject = null;
    public static Map<String, Integer> synchronizedMapObject = null;
    public static Map<String, Integer> concurrentHashMapObject = null;

    public static void performTest(final Map<String, Integer> threads) throws InterruptedException {

        System.out.println("Test started for: " + threads.getClass());
        long averageTime = 0;

        for (int i = 0; i < 5; i++) {

            long startTime = System.nanoTime();
            ExecutorService exServer = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (int j = 0; j < THREAD_POOL_SIZE; j++) {

                exServer.execute(new Runnable() {
                    @SuppressWarnings("unused")
                    //@Override
                    public void run() {

                        for (int i = 0; i < 500000; i++) {
                            Integer randomNumber = (int) Math.ceil(Math.random() * 550000);

                            // Retrieve value. We are not using it anywhere
                            Integer value = threads.get(String.valueOf(randomNumber));

                            // Put value
                            threads.put(String.valueOf(randomNumber), randomNumber);
                        }
                    }
                });
            }

            // Make sure executor stops
            exServer.shutdown();

            // Blocks until all tasks have completed execution after a shutdown request
            exServer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

            long entTime = System.nanoTime();
            long totalTime = (entTime - startTime) / 1000000L;
            averageTime += totalTime;
            System.out.println("2500K entried added/retrieved in " + totalTime + " ms");
        }

        System.out.println("For " + threads.getClass() + " the average time is " + averageTime / 5 + " ms\n");
    }

    public static void main(String[] args) throws InterruptedException {

        // Test with Hashtable Object
        hashTableObject = new Hashtable<String, Integer>();
        performTest(hashTableObject);

        // Test with synchronizedMap Object
        synchronizedMapObject = Collections.synchronizedMap(new HashMap<String, Integer>());
        performTest(synchronizedMapObject);

        // Test with ConcurrentHashMap Object
        concurrentHashMapObject = new ConcurrentHashMap<String, Integer>();
        performTest(concurrentHashMapObject);
    }
}


