

class Child1 implements Runnable {

    Object mutex;

    public Child1(Object mutex) {
        this.mutex = mutex;
    }

    public void run() {

        synchronized (mutex) {

            for (int c = 0; c < 10; c++) {
                System.out.println(c + 1);
            }

            try {
                mutex.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int c = 20; c < 30; c++) {
            System.out.println(c + 1);
        }
    }
}


class Child2 implements Runnable {

    Object mutex;

    public Child2(Object mutex) {
        this.mutex = mutex;
    }

    public void run() {
        synchronized (mutex) {
            for (int c = 11; c < 21; c++) {
                System.out.println(c);
            }
            mutex.notify(); // Changed here
        }

    }
}


public class App {


    public static void main(String[] args) throws InterruptedException {

        Object mutex = 1;

        Thread child1 = new Thread(new Child1(mutex));
        Thread child2 = new Thread(new Child2(mutex));

        child1.start();
        child2.start();
    }
}
