
import java.util.concurrent.Exchanger;


/*
 * The java.util.concurrent.Exchanger class represents a kind 
 * of rendezvous point where two threads can exchange objects.
 * */
public class App {


    private static class ExThread implements Runnable {


        Exchanger exchanger = null;
        Object object = null;

        public ExThread(Exchanger exchanger, Object object) {
            this.exchanger = exchanger;
            this.object = object;
        }

        public void run() {

            try {
                Object previous = this.object;

                this.object = this.exchanger.exchange(this.object);
                System.out.println(Thread.currentThread().getName() + " exchanged " + previous + " for " + this.object);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {

        Exchanger exchanger = new Exchanger();

        ExThread thread1 = new ExThread(exchanger, "A");
        ExThread thread2 = new ExThread(exchanger, "B");

        new Thread(thread1).start();
        new Thread(thread2).start();

        try {

            new Thread(thread1).join();
            new Thread(thread2).join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        System.out.println(thread1.object.toString());
    }
}