
/*
The ThreadLocal class in Java enables you to create variables that can only
be read and written by the same thread. Thus, even if two threads are executing
the same code, and the code has a reference to a ThreadLocal variable, then
the two threads cannot see each other's ThreadLocal variables.

                private ThreadLocal myThreadLocal = new ThreadLocal();

As you can see, you instantiate a new ThreadLocal object. This only needs to be
done once per thread. Even if different threads execute the same code which
accesses a ThreadLococal, each thread will see only its own ThreadLocal instance.
Even if two different threads set different values on the same ThreadLocal object,
they cannot see each other's values.


Since values set on a ThreadLocal object only are visible to the thread who set
the value, no thread can set an initial value on a ThreadLocal using set() which
is visible to all threads.

Instead you can specify an initial value for a ThreadLocal object by subclassing
ThreadLocal and overriding the initialValue() method.

        private ThreadLocal myThreadLocal = new ThreadLocal<String>() {
            @Override protected String initialValue() {
                return "This is the initial value";
            }
        };
* */
public class ThreadLocalExample {


    public static class MyRunnable implements Runnable {

        private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

        @Override
        public void run() {
            threadLocal.set((int) (Math.random() * 100D));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            System.out.println("The thread local value = "+threadLocal.get());
        }
    }


    public static void main(String[] args) {

        MyRunnable runnable = new MyRunnable();

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        thread1.start();
        thread2.start();

        try {
            thread1.join(); //wait for thread 1 to terminate
            thread2.join(); //wait for thread 2 to terminate
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
