package Multithreading.ProducerConsumer;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Chaklader on 1/15/17.
 */


/*
The producer-consumer problem (also known as the bounded-buffer problem) is a classic Java Example of a multi-process
synchronization problem.

The problem describes two processes, the producer and the consumer, who share a common, fixed-size buffer used as a queue.
The producer’s job is to generate a piece of data, put it into the buffer and start again.

At the same time, the consumer is consuming the data (i.e., removing it from the buffer) one piece at a time. The problem
is to make sure that the producer won’t try to add data into the buffer if it’s full and that the consumer won’t try to remove
data from an empty buffer.
* */

public class CrunchifyProducerConsumer {
    private static Vector<Object> data = new Vector<Object>();

    public static void main(String[] args) {
        new Producer().start();
        new Consumer().start();
    }

    public static class Consumer extends Thread {
        Consumer() {
            super("Consumer");
        }

        @Override
        public void run() {
            for (;;) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                @SuppressWarnings("rawtypes")
                Iterator it = data.iterator();
                while (it.hasNext())
                    it.next();
            }
        }
    }

    public static class Producer extends Thread {
        Producer() {
            super("Producer");
        }

        @Override
        public void run() {
            for (;;) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                data.add(new Object());
                if (data.size() > 1000)
                    data.remove(data.size() - 1);
            }
        }
    }
}




/*Add the keyword synchronized  to put a lock on the data  while we are using it.
*/

class CrunchifyProducerConsumer1 {

    private static Vector<Object> data = new Vector<Object>();

    public static void main(String[] args) {
        new Producer().start();
        new Consumer().start();
    }

    public static class Consumer extends Thread {
        Consumer() {
            super("Consumer");
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run() {
            for (;;) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Object Consumed ################");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (data) {
                    Iterator it = data.iterator();
                    while (it.hasNext())
                        it.next();
                }
            }
        }
    }

    public static class Producer extends Thread {
        Producer() {
            super("Producer");
        }

        @Override
        public void run() {
            for (;;) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Object Produced ~~~~~~~~~~~~~~~");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                data.add(new Object());
                if (data.size() > 1000)
                    data.remove(data.size() - 1);
            }
        }
    }
}
