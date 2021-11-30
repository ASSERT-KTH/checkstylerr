-----------------------------------------------------------------------------------------
package com.baeldung.forkjoin.util;

import java.util.concurrent.ForkJoinPool;

/*

The fork/join framework was presented in Java 7. It provides tools to 
help speed up parallel processing by attempting to use all available 
processor cores – which is accomplished through a divide and conquer approach.

In practice, this means that the framework first “forks”, recursively breaking 
the task into smaller independent subtasks until they are simple enough to be 
executed asynchronously.

After that, the “join” part begins, in which results of all subtasks are recursively 
joined into a single result, or in the case of a task which returns void, the program 
simply waits until every subtask is executed.

To provide effective parallel execution, the fork/join framework uses a pool of threads 
called the ForkJoinPool, which manages worker threads of type ForkJoinWorkerThread. 

The "ForkJoinPool" is the heart of the framework. It is an implementation of the 
ExecutorService that manages worker threads and provides us with tools to get 
information about the thread pool state and performance.

Worker threads can execute only one task at the time, but the ForkJoinPool doesn’t 
create a separate thread for every single subtask. Instead, each thread in the pool 
has its own double-ended queue (or DEQUE, pronounced deck) which stores tasks.

This architecture is vital for balancing the thread’s workload with the help of the 
work-stealing algorithm.

-----------------------
Work Stealing Algorithm
-----------------------

By default, a worker thread gets tasks from the head of its own deque. When it is empty, 
the thread takes a task from the tail of the deque of another busy thread or from the 
global entry queue, since this is where the biggest pieces of work are likely to be 
located.

This approach minimizes the possibility that threads will compete for tasks. It also 
reduces the number of times the thread will have to go looking for work, as it works 
on the biggest available chunks of work first.

ForkJoinPool Instantiation
--------------------------

In Java 8, the most convenient way to get access to the instance of the ForkJoinPool 
is to use its static method commonPool(). As its name suggests, this will provide a 
reference to the common pool, which is a default thread pool for every ForkJoinTask.

Using the predefined common pool reduces resource consumption, since this discourages 
the creation of a separate thread pool per task.

	ForkJoinPool commonPool = ForkJoinPool.commonPool();

The same behavior can be achieved in Java 7 by creating a ForkJoinPool and assigning 
it to a public static field of a utility class. With ForkJoinPool’s constructors, it 
is possible to create a custom thread pool with a specific level of parallelism, thread 
factory, and exception handler. In the example above, the pool has a parallelism level 
of 2. This means that pool will use 2 processor cores.


	public static ForkJoinPool forkJoinPool = new ForkJoinPool(2);

	ForkJoinPool forkJoinPool = PoolUtil.forkJoinPool;

-----------
Suggestions 
-----------

i. Use as few thread pools as possible – in most cases, the best decision is to 
use one thread pool per application or system

ii. Use the default common thread pool, if no specific tuning is needed

iii. Use a reasonable threshold for splitting ForkJoingTask into subtasks

iv. Avoid any blocking in your ForkJoingTasks */
-----------------------------------------------------------------------------------------

public class PoolUtil {

    public static ForkJoinPool forkJoinPool = new ForkJoinPool(2);
}




