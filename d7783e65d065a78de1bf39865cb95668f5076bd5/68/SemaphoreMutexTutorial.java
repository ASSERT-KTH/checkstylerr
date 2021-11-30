package Multithreading.SemaphoreMutex;

/**
 * Created by Chaklader on 1/15/17.
 */

//What is Java Semaphore and Mutex – Java Concurrency MultiThread explained with Example

/*
Take an example of Shopper and Customer
Shopper is borrowing Laptops
Customer can come and use Laptop – customer need a key to use a Laptop
After use – customer can return Laptop to Shopper

What is Mutex (Just 1 thread):

Shopper has a key to a Laptop. One customer can have the key – borrow a Laptop – at the time. When task finishes, the Shopper gives (frees) the key to the next customer in the queue.

Official Definition: “Mutexes are typically used to serialise access to a section of re-entrant code that cannot be executed concurrently by more than one thread. A mutex object only allows one thread into a controlled section, forcing other threads which attempt to gain access to that section to wait until the first thread has exited from that section.”

In other words: Mutex = Mutually Exclusive Semaphore

What is Semaphore (N specified threads):

Let’s say now Shopper has 3 identical Laptops and 3 identical keys. Semaphore is the number of free identical Laptop keys. The semaphore count – the count of keys – is set to 3 at beginning (all three Laptops are free), then the count value is decremented as Customer are coming in. If all Laptops are in use, i.e. there are no free keys left for Laptop, the semaphore count is 0. Now, when any of the customer returns the Laptop, semaphore is increased to 1 (one free key), and given to the next customer in the queue.

Official Definition: “A semaphore restricts the number of simultaneous users of a shared resource up to a maximum number. Threads can request access to the resource (decrementing the semaphore), and can signal that they have finished using the resource (incrementing the semaphore).”

Another must read: Lazy Creation of Singleton ThreadSafe Instance


// SemaphoreMutex1
// ---------------


In above tutorial CrunchifySemaphoreMutexTutorial.java when the CrunchifyProducer adds threadName to crunchifyList
linkedList object it can signal the semaphore. The CrunchifyConsumer can then be trying to acquire the semaphore so
they will be waiting until the CrunchifyProducer has signalled a threadID has been added. Upon signalling a added data,
one of the consumers will be woken and it will know it can read a crunchifyList Object. It can read a list, then go back
to trying to acquire on the semaphore. If in that time the producer has written another packet it has signalled again and
either of the consumers will then go on to read another packet and so on…


// SemaphoreMutex2
// ---------------

How to prevent race condition:

What if you have multiple Consumers? In above Java Tutorial The consumers (not the producer) should lock the buffer
when reading the packet (but not when acquiring the semaphore) to prevent race conditions. In the example below the
producer also locks the list since everything is on the same JVM.


* */


import java.util.LinkedList;
import java.util.concurrent.Semaphore;


class SemaphoreMutex1 {

    static Object crunchifyLock = new Object();
    static LinkedList<String> crunchifyList = new LinkedList<String>();

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
                    crunchifyList.add(threadName);
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
                    for (String value : crunchifyList) {
                        result = value + ",";
                    }
                    System.out.println(consumerName + " consumes value: " + result + "crunchifyList.size(): "
                            + crunchifyList.size() + "\n");
                    mutex.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


class SemaphoreMutex2 {

    private static final int MAX_CONCURRENT_THREADS = 2;
    private final Semaphore crunchifyAdminLOCK = new Semaphore(MAX_CONCURRENT_THREADS, true);

    public void crunchifyStartTest() {
        for (int i = 0; i < 10; i++) {
            CrunchifyPerson person = new CrunchifyPerson();
            person.start();
        }
    }

    public class CrunchifyPerson extends Thread {
        @Override
        public void run() {
            try {

                // Acquire Lock
                crunchifyAdminLOCK.acquire();
            } catch (InterruptedException e) {
                System.out.println("received InterruptedException");
                return;
            }
            System.out.println("Thread " + this.getId() + " start using Crunchify's car - Acquire()");
            try {
                sleep(1000);
            } catch (Exception e) {

            } finally {

                // Release Lock
                crunchifyAdminLOCK.release();
            }
            System.out.println("Thread " + this.getId() + " stops using Crunchify's car -  Release()\n");
        }
    }

}


public class SemaphoreMutexTutorial {

    public static void main(String[] args) {

        // 1
        //You have to have a reference to the other outer class as well.
        //Inner inner = new MyClass().new Inner();
        //If Inner was static then it would be
        //Inner inner = new MyClass.Inner();

        new SemaphoreMutex1.CrunchifyProducer().start();
        new SemaphoreMutex1.CrunchifyConsumer("Crunchify").start();
        new SemaphoreMutex1.CrunchifyConsumer("Google").start();
        new SemaphoreMutex1.CrunchifyConsumer("Yahoo").start();


        // 2
        SemaphoreMutex2 test = new SemaphoreMutex2();
        test.crunchifyStartTest();
    }
}
