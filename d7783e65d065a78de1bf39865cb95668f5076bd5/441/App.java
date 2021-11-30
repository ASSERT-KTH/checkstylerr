package com.balazsholczer.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*Tasks can be assigned to the ExecutorService using several methods, including 
execute(), which is inherited from the Executor interface, and also submit(), 
invokeAny(), invokeAll()

	a. The execute() method is void, and it doesn’t give any possibility to get the result 
	of task’s execution or to check the task’s status (is it running or executed).
		
		executorService.execute(runnableTask);

	b. submit() submits a Callable or a Runnable task to an ExecutorService and returns a
	result of type Future.

		Future<String> future = executorService.submit(callableTask);

	c. invokeAny() assigns a collection of tasks to an ExecutorService, causing each to be 
	executed, and returns the result of a successful execution of one task (if there was a 
	successful execution).

		String result = executorService.invokeAny(callableTasks);

	d. invokeAll() assigns a collection of tasks to an ExecutorService, causing each to 
	be executed, and returns the result of all task executions in the form of a list of 
	objects of type Future.
		List<Future<String>> futures = executorService.invokeAll(callableTasks);


In general, the ExecutorService will not be automatically destroyed when there is not 
task to process. It will stay alive and wait for new work to do.

In some cases this is very helpful; for example, if an app needs to process tasks 
which appear on an irregular basis or the quantity of these tasks is not known at 
compile time.

On the other hand, an app could reach its end, but it will not be stopped because 
a waiting ExecutorService will cause the JVM to keep running. To properly shut down 
an ExecutorService, we have the shutdown() and shutdownNow() APIs.

The shutdown() method doesn’t cause an immediate destruction of the ExecutorService. 
It will make the ExecutorService stop accepting new tasks and shut down after all 
running threads finish their current work.

	executorService.shutdown();

The shutdownNow() method tries to destroy the ExecutorService immediately, but it 
doesn’t guarantee that all the running threads will be stopped at the same time. 
This method returns a list of tasks which are waiting to be processed. It is up to 
the developer to decide what to do with these tasks.

	List<Runnable> notExecutedTasks = executorService.shutDownNow();

One good way to shut down the ExecutorService (which is also recommended by Oracle) 
is to use both of these methods combined with the awaitTermination() method. With 
this approach, the ExecutorService will first stop taking new tasks, the wait up to 
a specified period of time for all tasks to be completed. If that time expires, the 
execution is stopped immediately:


	executorService.shutdown();

	try {
	    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
	        executorService.shutdownNow();
	    } 
	} catch (InterruptedException e) {
	    executorService.shutdownNow();
	}


The Future Interface
--------------------

The submit() and invokeAll() methods return an object or a collection of objects 
of type Future, which allows us to get the result of a task’s execution or to check 
the task’s status (is it running or executed).

The Future interface provides a special blocking method get() which returns an actual 
result of the Callable task’s execution or null in the case of Runnable task. Calling 
the get() method while the task is still running will cause execution to block until 
the task is properly executed and the result is available.	

	Future<String> future = executorService.submit(callableTask);

	String result = null;

	try {
	    result = future.get();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	}


With very long blocking caused by the get() method, an application’s performance can 
degrade. If the resulting data is not crucial, it is possible to avoid such a problem 
by using timeouts:

	String result = future.get(200, TimeUnit.MILLISECONDS);

If the execution period is longer than specified (in this case 200 milliseconds), 
a TimeoutException will be thrown.The isDone() method can be used to check if the 
assigned task is already processed or not. The Future interface also provides for 
the cancellation of task execution with the cancel() method, and to check the 
cancellation with isCancelled() method:

	boolean canceled = future.cancel(true);
	boolean isCancelled = future.isCancelled();





The ScheduledExecutorService Interface
--------------------------------------

The ScheduledExecutorService runs tasks after some predefined delay and/or 
periodically. Once again, the best way to instantiate a ScheduledExecutorService 
is to use the factory methods of the Executors class. For this section, a 
ScheduledExecutorService with one thread will be used:

	ScheduledExecutorService executorService = Executors
													.newSingleThreadScheduledExecutor();

To schedule a single task’s execution after a fixed delay, us the scheduled() 
method of the ScheduledExecutorService. There are two scheduled() methods that 
allow you to execute Runnable or Callable tasks. The scheduleAtFixedRate() method 
lets execute a task periodically after a fixed delay. The code below delays for 
one second before executing callableTask. 

	Future<String> resultFuture = executorService.schedule(callableTask, 1, TimeUnit.SECONDS);

The code will execute a task after an initial delay of 100 milliseconds, and after that, 
it will execute the same task every 450 milliseconds. If the processor needs more time 
to execute an assigned task than the period parameter of the scheduleAtFixedRate() method, 
the ScheduledExecutorService will wait until the current task is completed before starting 
the next:

	Future<String> resultFuture = service.scheduleAtFixedRate(runnableTask, 100, 450, 
																TimeUnit.MILLISECONDS);

If it is necessary to have a fixed length delay between iterations of the task, 
scheduleWithFixedDelay() should be used. For example, the following code will 
guarantee a 150-millisecond pause between the end of the current execution and 
the start of another one. 

	service.scheduleWithFixedDelay(task, 100, 150, TimeUnit.MILLISECONDS);

According to the scheduleAtFixedRate() and scheduleWithFixedDelay() method contracts, 
period execution of the task will end at the termination of the ExecutorService or 
if an exception is thrown during task execution.   



ExecutorService vs. Fork/Join
-----------------------------

ExecutorService gives the developer the ability to control the number of generated 
threads and the granularity of tasks which should be executed by separate threads. 
The best use case for ExecutorService is the processing of independent tasks, such 
as transactions or requests according to the scheme “one thread for one task.”

In contrast, according to Oracle’s documentation, fork/join was designed to speed up 
work which can be broken into smaller pieces recursively.



Conclusion
----------

Even despite the relative simplicity of ExecutorService, there are a few common 
pitfalls. Let’s summarize them:

	a. Keeping an unused ExecutorService alive: There is a detailed explanation in 
	section 4 of this article about how to shut down an ExecutorService; 

	b. Wrong thread-pool capacity while using fixed length thread-pool: It is very 
	important to determine how many threads the application will need to execute 
	tasks efficiently. A thread-pool that is too large will cause unnecessary overhead 
	just to create threads which mostly will be in the waiting mode. Too few can make 
	an application seem unresponsive because of long waiting periods for tasks in 
	the queue.

	c. Calling a Future‘s get() method after task cancellation: An attempt to get the 
	result of an already canceled task will trigger a CancellationException.

	d. Unexpectedly-long blocking with Future‘s get() method: Timeouts should be used 
	to avoid unexpected waits.
*/


/**
 *  1.) ExecutorService es = Executors.newCachedThreadPool();
 *
 *  	- going to return an executorService that can dynamically reuse threads
 *  		
 *		- before starting a job -> it going to check whether there are any threads that
 *			finished the job...reuse them
 *		- if there are no waiting threads -> it is going to create another one 
 *		- good for the processor ... effective solution !!!
 *
 *	2.) ExecutorService es = Executors.newFixedThreadPool(N);
 *		- maximize the number of threads
 *		- if we want to start a job -> if all the threads are busy, we have to wait for one
 *			to terminate
 *
 *	3.) ExecutorService es = Executors.newSingleThreadExecutor();
 *		It uses a single thread for the job
 *
 *		execute() -> runnable + callable
 *		submit() -> runnable
 */



class Worker implements Runnable {


		@Override
		public void run() {

			for(int i=0;i<10;i++){

				System.out.println(i);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
}


public class App {


	public static void main(String[] args) {
		
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		
		for(int i=0;i<5;i++){
			executorService.execute(new Worker());
		}		
	}
}
