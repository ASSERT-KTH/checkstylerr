package com.balazsholczer.thread;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;




public class BlockingQueue<T> {


    private Queue<T> queue = new LinkedList<T>();
    private int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void put(T element) throws InterruptedException {
        while(queue.size() == capacity) {
            wait();
        }

        queue.add(element);
        notify(); // notifyAll() for multiple producer/consumer threads
    }

    public synchronized T take() throws InterruptedException {
        while(queue.isEmpty()) {
            wait();
        }

        T item = queue.remove();
        notify(); // notifyAll() for multiple producer/consumer threads
        return item;
    }
}



/*
* Java 1.5 introduced a new concurrency library (in the java.util.concurrent 
* package) which was designed to provide a higher level abstraction over the 
* wait/notify mechanism. 
*/
public class BlockingQueue2<T> {


	private int capacity;

    private Queue<T> queue = new LinkedList<T>();

    // initiate a ReentrantLock lock for 
    private Lock lock = new ReentrantLock();

    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public void put(T element) throws InterruptedException {

        lock.lock();

        try {
        	
            while(queue.size() == capacity) {
                notFull.await();
            }

            queue.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {

        lock.lock();
        
        try {

            while(queue.isEmpty()) {
                notEmpty.await();
            }

            T item = queue.remove();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }
}