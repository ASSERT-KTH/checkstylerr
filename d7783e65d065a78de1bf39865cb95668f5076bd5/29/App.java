package Semaphores_12;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/*Java multi threads example to show you how to use Semaphore and Mutex 
to limit the number of threads to access resources.

Semaphores – Restrict the number of threads that can access a resource. 
Example, limit max 10 connections to access a file simultaneously.

Mutex – Only one thread to access a resource at once. Example, when a 
client is accessing a file, no one else should have access the same file 
at the same time.*/


/*Before obtaining an item each thread must acquire a permit from the semaphore, 
guaranteeing that an item is available for use. When the thread has finished 
with the item it is returned back to the pool and a permit is returned to the 
semaphore, allowing another thread to acquire that item. Note that no synchronization 
lock is held when acquire() is called as that would prevent an item from being returned
to the pool. The semaphore encapsulates the synchronization needed to restrict access 
to the pool, separately from any synchronization needed to maintain the consistency of 
the pool itself.


A semaphore initialized to one, and which is used such that it only has at most one 
permit available, can serve as a mutual exclusion lock. This is more commonly known 
as a binary semaphore, because it only has two states: one permit available, or zero 
permits available. When used in this way, the binary semaphore has the property 
(unlike many Lock implementations), that the "lock" can be released by a thread other 
than the owner (as semaphores have no notion of ownership). This can be useful in some 
specialized contexts, such as deadlock recovery.

The constructor for this class optionally accepts a fairness parameter. When set false, 
this class makes no guarantees about the order in which threads acquire permits. In 
particular, barging is permitted, that is, a thread invoking acquire() can be allocated 
a permit ahead of a thread that has been waiting - logically the new thread places itself 
at the head of the queue of waiting threads. When fairness is set true, the semaphore 
guarantees that threads invoking any of the acquire methods are selected to obtain 
permits in the order in which their invocation of those methods was processed 
(first-in-first-out; FIFO). Note that FIFO ordering necessarily applies to specific 
internal points of execution within these methods. So, it is possible for one thread 
to invoke acquire before another, but reach the ordering point after the other, and 
similarly upon return from the method. Also note that the untimed  "tryAcquire"  methods 
do not honor the fairness setting, but will take any permits that are available.

Generally, semaphores used to control resource access should be initialized as fair, 
to ensure that no thread is starved out from accessing a resource. When using semaphores 
for other kinds of synchronization control, the throughput advantages of non-fair 
ordering often outweigh fairness considerations. This class also provides convenience methods to acquire and release multiple permits at 
a time. Beware of the increased risk of indefinite postponement when these methods are 
used without fairness set true. Memory consistency effects: Actions in a thread prior to calling a "release" method such 
as release() happen-before actions following a successful "acquire" method such as 
acquire() in another thread.*/


/**
 * {@link java.util.concurrent.Semaphore}s
 * are mainly used to limit the number of simultaneous threads that
 * can access a resources, but you can also use them to implement deadlock
 * recovery systems since a semaphore with one permit is basically a lock that
 * can unlock from other threads.
 * <br>
 * Source:
 * <a href="http://stackoverflow.com/questions/771347/what-is-mutex-and-semaphore-in-java-what-is-the-main-difference">
 * http://stackoverflow.com/questions/771347/what-is-mutex-and-semaphore-in-java-what-is-the-main-difference
 * </a>
 * <p>
 * Mutex (or a semaphore initialized to 1; meaning there's only one resource)
 * is basically a mutual exclusion; Only one thread can acquire the resource
 * at once, and all other threads trying to acquire the resource are blocked
 * until the thread owning the resource releases.
 * <p>
 * Semaphore is used to control the number of threads executing. There will be
 * fixed set of resources. The resource count will gets decremented every time
 * when a thread owns the same. When the semaphore count reaches 0 then no other
 * threads are allowed to acquire the resource. The threads get blocked till
 * other threads owning resource releases.
 * </p>
 * <p>
 *
 * In short, the main difference is how many threads are allowed to acquire the
 * resource at once.
 * TODO -- go a little more in depth explaining that
 * Mutex --its ONE. Semaphore -- its DEFINED_COUNT, ( as many as semaphore
 * count)
 */



public class App {


    public static void main(String[] args) throws Exception {

        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 20; i++) { //200 hundred times will be called

            executor.submit(new Runnable() {
                public void run() {
                    Connectionn.getInstance().connect();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }
}
