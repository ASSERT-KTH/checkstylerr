
/*
Clearly, notify wakes (any) one thread in the wait set, notifyAll wakes all threads in the waiting set. 
The following discussion should clear up any doubts. notifyAll should be used most of the time. If you 
are not sure which to use, then use notifyAll.Please see explanation that follows.

Read very carefully and understand. Please send me an email if you have any questions.

Look at producer/consumer (assumption is a ProducerConsumer class with two methods). IT IS BROKEN (because 
it uses notify) - yes it MAY work - even most of the time, but it may also cause deadlock - we will see 
why:
*/
public  class App {

	
	public synchronized void put(Object o) {

	    while (buf.size()==MAX_SIZE) {

	    	// called if the buffer is full (try/catch removed for brevity)
	        wait(); 
	    }

	    buf.add(o);

	    // called in case there are any getters or putters waiting
	    notify(); 
	}

	public synchronized Object get() {

	    // Y: this is where C2 tries to acquire the lock (i.e. at the beginning of the method)
	    while (buf.size()==0) {
	        wait(); // called if the buffer is empty (try/catch removed for brevity)
	        // X: this is where C1 tries to re-acquire the lock (see below)
	    }

	    Object o = buf.remove(0);
	    notify(); // called if there are any getters or putters waiting

	    return o;
	}

	public static void main(String[] args) {
		System.out.println("Miami");	
	}
}




/*

FIRSTLY,

Why do we need a while loop surrounding the wait?

We need a while loop in case we get this situation:

Consumer 1 (C1) enter the synchronized block and the buffer is empty, so C1 is put in the wait set 
(via the wait call). Consumer 2 (C2) is about to enter the synchronized method (at point Y above), 
but Producer P1 puts an object in the buffer, and subsequently calls notify. The only waiting thread 
is C1, so it is woken and now attempts to re-acquire the object lock at point X (above).

Now C1 and C2 are attempting to acquire the synchronization lock. One of them (nondeterministically) 

is chosen and enters the method, the other is blocked (not waiting - but blocked, trying to acquire 

the lock on the method). Let's say C2 gets the lock first. C1 is still blocking (trying to acquire 

the lock at X). C2 completes the method and releases the lock. Now, C1 acquires the lock. Guess what, 

lucky we have a while loop, because, C1 performs the loop check (guard) and is prevented from removing 

a non-existent element from the buffer (C2 already got it!). If we didn't have a while, we would get 

an IndexArrayOutOfBoundsException as C1 tries to remove the first element from the buffer!



NOW,

Ok, now why do we need notifyAll?

In the producer/consumer example above it looks like we can get away with notify. It seems this way, 

because we can prove that the guards on the wait loops for producer and consumer are mutually exclusive. 

That is, it looks like we cannot have a thread waiting in the put method as well as the get method, 

because, for that to be true, then the following would have to be true:



buf.size() == 0 AND buf.size() == MAX_SIZE (assume MAX_SIZE is not 0)

HOWEVER, this is not good enough, we NEED to use notifyAll. Let's see why ...

Assume we have a buffer of size 1 (to make the example easy to follow). The following steps lead us to 

deadlock. Note that ANYTIME a thread is woken with notify, it can be non-deterministically selected by 
the JVM - that is any waiting thread can be woken. Also note that when multiple threads are blocking on 

entry to a method (i.e. trying to acquire a lock), the order of acquisition can be non-deterministic. 

Remember also that a thread can only be in one of the methods at any one time - the synchronized methods 

allow only one thread to be executing (i.e. holding the lock of) any (synchronized) methods in the class. 




If the following sequence of events occurs - deadlock results:

STEP 1:
- P1 puts 1 char into the buffer

STEP 2:
- P2 attempts put - checks wait loop - already a char - waits

STEP 3:
- P3 attempts put - checks wait loop - already a char - waits

STEP 4:
- C1 attempts to get 1 char 
- C2 attempts to get 1 char - blocks on entry to the get method
- C3 attempts to get 1 char - blocks on entry to the get method

STEP 5:
- C1 is executing the get method - gets the char, calls notify, exits method

- The notify wakes up P2

- BUT, C2 enters method before P2 can (P2 must reacquire the lock), so P2 blocks on entry to the put 
method

- C2 checks wait loop, no more chars in buffer, so waits

- C3 enters method after C2, but before P2, checks wait loop, no more chars in buffer, so waits



STEP 6:

- NOW: there is P3, C2, and C3 waiting!
- Finally P2 acquires the lock, puts a char in the buffer, calls notify, exits method

STEP 7:

- P2's notification wakes P3 (remember any thread can be woken)
- P3 checks the wait loop condition, there is already a char in the buffer, so waits.
- NO MORE THREADS TO CALL NOTIFY and THREE THREADS PERMANENTLY SUSPENDED!

SOLUTION: Replace notify with notifyAll in the producer/consumer code (above).

*/



