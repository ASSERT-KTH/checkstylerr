package ReentrantLocks_10;





/*
Some important interfaces and classes in Java Lock API are:

a. Lock: This is the base interface for Lock API. It provides all the features of 
synchronized keyword with additional ways to create different Conditions for 
locking, providing timeout for thread to wait for lock. Some of the important 
methods are lock() to acquire the lock, unlock() to release the lock, tryLock() 
to wait for lock for a certain period of time, newCondition() to create the Condition 
etc.

b. Condition: Condition objects are similar to Object wait-notify model with additional 
feature to create different sets of wait. A Condition object is always created by Lock 
object. Some of the important methods are await() that is similar to wait() and signal(), 
signalAll() that is similar to notify() and notifyAll() methods.

c. ReadWriteLock: It contains a pair of associated locks, one for read-only operations 
and another one for writing. The read lock may be held simultaneously by multiple reader 
threads as long as there are no writer threads. The write lock is exclusive.

d. ReentrantLock: This is the most widely used implementation class of Lock interface. 
This class implements the Lock interface in similar way as synchronized keyword. Apart 
from Lock interface implementation, ReentrantLock contains some utility methods to get 
the thread holding the lock, threads waiting to acquire the lock etc.

synchronized block are reentrant in nature i.e if a thread has lock on the monitor object and if another synchronized block requires to have the lock on the same monitor object then thread can enter that code block. I think this is the reason for the class name to be ReentrantLock. Let’s understand this feature with a simple example.
*/


/*
Java Lock vs synchronized
-------------------------

Based on above details and program, we can easily conclude following differences 
between Java Lock and synchronization.

1. Java Lock API provides more visibility and options for locking, unlike synchronized 
where a thread might end up waiting indefinitely for the lock, we can use tryLock() to 
make sure thread waits for specific time only.

2. Synchronization code is much cleaner and easy to maintain whereas with Lock we are 
forced to have try-finally block to make sure Lock is released even if some exception 
is thrown between lock() and unlock() method calls.

3. synchronization blocks or methods can cover only one method whereas we can acquire 
the lock in one method and release it in another method with Lock API.

4. synchronized keyword doesn’t provide fairness whereas we can set fairness to true 
while creating ReentrantLock object so that longest waiting thread gets the lock first.

5. We can create different conditions for Lock and different thread can await() for 
different conditions.
*/



// ReentrantLock 
// -------------

/*A reentrant mutual exclusion Lock with the same basic behavior and semantics as 
the implicit monitor lock accessed using synchronized methods and statements, but 
with extended capabilities.

A ReentrantLock is owned by the thread last successfully locking, but not yet unlocking 
it. A thread invoking lock will return, successfully acquiring the lock, when the lock 
is not owned by another thread. The method will return immediately if the current thread 
already owns the lock. This can be checked using methods isHeldByCurrentThread(), and 
getHoldCount().

The constructor for this class accepts an optional fairness parameter. When set true, 
under contention, locks favor granting access to the longest-waiting thread. Otherwise 
this lock does not guarantee any particular access order. Programs using fair locks 
accessed by many threads may display lower overall throughput (i.e., are slower; often 
much slower) than those using the default setting, but have smaller variances in times 
to obtain locks and guarantee lack of starvation. 

Note however, that fairness of locks does not guarantee fairness of thread scheduling. 
Thus, one of many threads using a fair lock may obtain it multiple times in succession 
while other active threads are not progressing and not currently holding the lock. Also 
note that the untimed tryLock method does not honor the fairness setting. It will succeed 
if the lock is available even if other threads are waiting.*/


/**
 * The {@link java.util.concurrent.locks.ReentrantLock} class in Java as an
 * alternative to synchronized code blocks.
 * <br>
 * {@link java.util.concurrent.locks.ReentrantLock}s let you do all the
 * stuff that you can do with {@code synchronized}, {@link Object#wait()} and
 * {@link Object#notify()}, plus some more stuff. Besides that may come in
 * handy from time to time.
 * <br><br>
 * Source:<em>
 * http://docs.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/locks/ReentrantLock.html
 * </em>
 * <br><br>
 * {@link java.util.concurrent.locks.ReentrantLock} Extended capabilities
 * include:
 * <br>
 * <ul>
 * <li>
 * The ability to have more than one {@link java.util.concurrent.locks.Condition}
 * variable per monitor.
 * </li>
 * <li> Monitors that use the synchronized keyword can only have one. This means
 * {@link java.util.concurrent.locks.ReentrantLock}s support more than one
 * {@link Object#wait()}/{@link Object#notify()} queue.
 * </li>
 * <li>
 * The ability to make the lock "fair".
 * <em>
 * "[fair] locks favor granting access to the longest-waiting
 * thread. Otherwise this lock does not guarantee any particular access order."
 * </em>
 * </li>
 * <li> Synchronized blocks are unfair.</li>
 * <li> The ability to check if the lock is being
 * held.</li>
 * <li> The ability to get the list of threads waiting on the lock.</li>
 * </ul>
 * <br><br>
 * The disadvantages of {@link java.util.concurrent.locks.ReentrantLock}s are:
 * <br>
 * <ul>
 * <li> Need to add import statement.</li>
 * <li> Need to wrap lock acquisitions in a try/finally block. This makes it more
 * ugly than the synchronized keyword.</li>
 *
 *
 * <li>The synchronized keyword can be put in method definitions which avoids
 * the need for a block which reduces nesting.</li>
 * </ul>
 * <br><br>
 * For more complete comparison of
 * {@link java.util.concurrent.locks.ReentrantLock}s and {@code synchronized}
 * see:<em>
 * http://guruzon.com/1/concurrency/explicit-lock-locking/difference-between-synchronized-and-reentrantlock-in-java
 * </em>
 * <br><br>
 * Codes with minor comments are from
 * <a href="http://www.caveofprogramming.com/youtube/">
 * <em>http://www.caveofprogramming.com/youtube/</em>
 * </a>
 * <br>
 * also freely available at
 * <a href="https://www.udemy.com/java-multithreading/?couponCode=FREE">
 *     <em>https://www.udemy.com/java-multithreading/?couponCode=FREE</em>
 * </a>
 *
 * @author Z.B. Celik <celik.berkay@gmail.com>
 */




public class App {


    public static void main(String[] args) throws Exception {

        final Runner runner = new Runner();

        Thread t1 = new Thread(new Runnable() {

            public void run() {

                try {
                    runner.firstThread();
                } catch (InterruptedException ignored) {
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {

            public void run() {
                try {
                    runner.secondThread();
                } catch (InterruptedException ignored) {
                }
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        }
        
        catch (InterruptedException ie) {
            System.out.println("ie");
        }

        runner.finished();
    }
}
