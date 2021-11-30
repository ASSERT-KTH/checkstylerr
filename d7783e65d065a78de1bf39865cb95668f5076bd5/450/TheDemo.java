
// public class SOP {

//     public static void print(String s) {
//         System.out.println(s+"\n");
//     }
// }


/*
Note: synchronized blocks the next thread's call to method test() as long as the previous 
thread's execution is not finished. Threads can access this method one at a time. Without 
synchronized all threads can access this method simultaneously.

When a thread calls the synchronized method 'test' of the object (here object is an instance 
of 'TheDemo' class) it acquires the lock of that object, any new thread cannot call ANY 
synchronized method of the same object as long as previous thread which had acquired the 
lock does not release the lock.

Similar thing happens when any static synchronized method of the class is called. The thread 
acquires the lock associated with the class(in this case any non static synchronized method 
of an instance of that class can be called by any thread because that object level lock is 
still available). Any other thread will not be able to call any static synchronized method 
of the class as long as the class level lock is not released by the thread which currently 
holds the lock.
*/


public class TestThread extends Thread {

    String name;
    TheDemo theDemo;

    public TestThread(String name,TheDemo theDemo) {
        this.theDemo = theDemo;
        this.name = name;
        start();
    }

    @Override
    public void run() {
        theDemo.test(name);
    }
}




public class TheDemo {


    public synchronized void test(String name) {

        for(int i=0;i<10;i++) {

            System.out.println(name + " :: "+i);

            try{
                Thread.sleep(500);
            } catch (Exception e) {                
                System.out.println(e.getMessage());
            }
        }
    }


    public static void main(String[] args) {

        TheDemo theDemo = new TheDemo();

        new TestThread("THREAD 1",theDemo);
        new TestThread("THREAD 2",theDemo);
        new TestThread("THREAD 3",theDemo);
    }
}