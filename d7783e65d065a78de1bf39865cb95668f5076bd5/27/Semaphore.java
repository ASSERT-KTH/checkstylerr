package com.multi;

import java.util.concurrent.Semaphore;



public class Semaphore {

    private static final int MAX_CONCURRENT_THREADS = 2;
    private final Semaphore adminLOCK = new Semaphore(MAX_CONCURRENT_THREADS, true);
    
    public void startTest() {
        for (int i = 0; i < 10; i++) {
            Person person = new Person();
            person.start();
        }
    }
    
    public static void main(String[] args) {
        Semaphore test = new Semaphore();
        test.startTest();
        
    }
}



// a person is using Car  
class Person extends Thread {

        @Override
        public void run() {
            
            try {                
                // Acquire Lock
                adminLOCK.acquire();
            } catch (InterruptedException e) {
                System.out.println("received InterruptedException");
                return;
            }

            System.out.println("Thread " + this.getId() + " start using car - Acquire()");

            try {
                sleep(1000);
            } catch (Exception e) {
                
            } finally {
                
                // Release Lock
                adminLOCK.release();
            }
            System.out.println("Thread " + this.getId() + " stops using  car -  Release()\n");
        }
    }
