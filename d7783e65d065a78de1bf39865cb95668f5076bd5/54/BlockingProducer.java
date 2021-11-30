package Multithreading.TsBlockingQueue;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Chaklader on 1/15/17.
 */

/*Create producer CrunchifyBlockingProducer.java which created simple msg and put it into queue.*/
public class BlockingProducer implements Runnable {

    private BlockingQueue<Message> crunchQueue;

    public BlockingProducer(BlockingQueue<Message> queue) {
        this.crunchQueue = queue;
    }

    //    @Override
    public void run() {

        // producing CrunchifyMessage messages
        for (int i = 1; i <= 5; i++) {
            Message msg = new Message("i'm msg " + i);
            try {
                Thread.sleep(10);
                crunchQueue.put(msg);
                System.out.println("CrunchifyBlockingProducer: Message - " + msg.getMsg() + " produced.");
            } catch (Exception e) {
                System.out.println("Exception:" + e);
            }
        }

        // adding exit message
        Message msg = new Message("All done from Producer side. Produced 50 CrunchifyMessages");
        try {
            crunchQueue.put(msg);
            System.out.println("CrunchifyBlockingProducer: Exit Message - " + msg.getMsg());
        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }
    }
}