package WaitAndNotify_8;

import java.util.Scanner;


/**
 * Source: <em>http://www.programcreek.com/2009/02/notify-and-wait-example/</em>
*/


public class Processor {

    /*
     * public synchronized void getSomething(){ this.hello = "hello World"; }
     * public void getSomething(){ synchronized(this){ this.hello = "hello
     * World"; } }
     * two code blocks by specification, functionally identical.
     */

    public void produce() throws InterruptedException {

        synchronized (this) {
            System.out.println("Producer thread running ....");
            wait();//this.wait() is fine.
            System.out.println("Resumed.");
        }
    }

    public void consume() throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        Thread.sleep(2000);

        synchronized (this) {

            System.out.println("Waiting for return key.");
            scanner.nextLine();
            System.out.println("Return key pressed.");

            notify();

            Thread.sleep(5000);
            System.out.println("Consumption done.");
        }
    }
}
