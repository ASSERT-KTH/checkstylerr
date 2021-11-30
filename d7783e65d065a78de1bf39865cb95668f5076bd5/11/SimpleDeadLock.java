package Deadlock_11;


/* Deadlock: A state of execution when 2 or more threads are all put on hold, because 
each of them is holding a synchronization lock while waiting for another lock. The
lock each thread is waiting for is held by one of the other threads. So none of threads 
can move forward.

By definition, deadlock can only happen when the program is running multiple threads, 
and multiple locks are being used by multiple threads. Therefore:

A single-threaded program will never have deadlocks.
A program with one lock will never have deadlocks.
Here is a simple program to demonstrate a deadlock with two threads and 
two locks:*/


public class SimpleDeadLock {


    public static final Object lock1 = new Object();
    public static final Object lock2 = new Object();

    private int index;

    public static void main(String[] a) {

        Thread t1 = new Thread1();
        Thread t2 = new Thread2();

        t1.start();
        t2.start();
    }


    private static class Thread1 extends Thread {

        public void run() {

            synchronized (lock1) {
                
                System.out.println("Thread 1: Holding lock 1...");
                
                try {
                    Thread.sleep(10);
                } 

                catch (InterruptedException ignored) {

                }

                System.out.println("Thread 1: Waiting for lock 2...");

                synchronized (lock2) {
                    System.out.println("Thread 2: Holding lock 1 & 2...");
                }
            }
        }
    }

    private static class Thread2 extends Thread {

        public void run() {
            
            synchronized (lock2) {
               
                System.out.println("Thread 2: Holding lock 2...");
                try {
                    Thread.sleep(10);
                } 

                catch (InterruptedException ignored) {
                    // some code 
                }

                System.out.println("Thread 2: Waiting for lock 1...");
                
                synchronized (lock1) {
                    System.out.println("Thread 2: Holding lock 2 & 1...");
                }
            }
        }
    }
}
