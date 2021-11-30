package Multithreading.TsBlockingQueue;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Chaklader on 1/15/17.
 */


/*Create class CrunchifyBlockingConsumer.java which consumes message from queue.*/
public class BlockingConsumer implements Runnable {

    private BlockingQueue<Message> queue;

    public BlockingConsumer(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    //    @Override
    public void run() {
        try {
            Message msg;

            // consuming messages until exit message is received
            while ((msg = queue.take()).getMsg() != "exit") {
                Thread.sleep(10);
                System.out.println("CrunchifyBlockingConsumer: Message - " + msg.getMsg() + " consumed.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}