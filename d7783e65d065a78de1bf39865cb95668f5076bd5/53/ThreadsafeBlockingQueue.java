package Multithreading.TsBlockingQueue; /**
 * Created by Chaklader on 1/15/17.
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


//What is Threadsafe BlockingQueue in Java and when you should use it? Implementation Attached

/* Create simple CrunchifyBlockingMain.java method which runs the
BlockingQueue test. Run this program to check BlockingQueue behavior.*/

public class ThreadsafeBlockingQueue {

    public static void main(String[] args) {

        // Creating BlockingQueue of size 10
        // BlockingQueue supports operations that wait for the queue to become non-empty when retrieving an element, and
        // wait for space to become available in the queue when storing an element.
        BlockingQueue<Message> crunchQueue = new ArrayBlockingQueue<Message>(10);
        BlockingProducer crunchProducer = new BlockingProducer(crunchQueue);
        BlockingConsumer crunchConsumer = new BlockingConsumer(crunchQueue);

        // starting producer to produce messages in queue
        new Thread(crunchProducer).start();

        // starting consumer to consume messages from queue
        new Thread(crunchConsumer).start();

        System.out.println("Let's get started. Producer / Consumer Test Started.\n");
    }
}

