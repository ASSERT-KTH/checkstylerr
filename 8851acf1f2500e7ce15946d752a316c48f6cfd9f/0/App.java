package com.sample;


public class App {


    /*
     * If a resource is created, used and disposed within the control of the
     * same thread, and never escapes the control of this thread, the use of
     * that resource is thread safe.
     *
     * This could also happen with threads operating on files or other shared
     * resources. Therefore it is important to distinguish between whether an
     * object controlled by a thread is the resource, or if it merely references
     * the resource (like a database connection does). Resources can be any shared
     * resource like an object, array, file, database connection, socket etc. In
     * Java you do not always explicitly dispose objects, so "disposed" means
     * losing or null'ing the reference to the object. Even if the use of an
     * object is thread safe, if that object points to a shared resource like a
     * file or database, your application as a whole may not be thread safe.
     * For instance, if thread 1 and thread 2 each create their own database
     * connections, connection 1 and connection 2, the use of each connection
     * itself is thread safe. But the use of the database the connections point
     * to may not be thread safe.
     * */

    public static void main(String[] args) {

//        System.out.println("Miami");

        NotThreadSafe sharedInstance = new NotThreadSafe();

        Thread t1 = new Thread(new MyRunnable(sharedInstance));
        Thread t2 = new Thread(new MyRunnable(sharedInstance));

        t1.start();
        t2.start();

        try {
            
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /*
         * if two threads call the add() method simultaneously on
         * different instances then it does not lead to race condition.
         * */

//      Thread t1 = new Thread(new MyRunnable(new NotThreadSafe())).start();
//      Thread t2 = new Thread(new MyRunnable(new NotThreadSafe())).start();


        System.out.println(sharedInstance.toString());
    }
}