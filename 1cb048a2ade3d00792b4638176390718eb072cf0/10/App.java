package com.balazsholczer.threads;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;




/*
According to Wikipedia, Asynchronous programming is a means of parallel programming 
in which a unit of work runs separately from the main application thread and notifies 
the calling thread of its completion, failure or progress.


Concurrency and Parallelism
---------------------------

Concurrency is essentially applicable when we talk about two tasks or more. 
When an application is capable of executing two tasks virtually at same time, 
we call it concurrent application. Though here tasks run looks like simultaneously, 
but essentially they may not. They take advantage of CPU time-slicing feature of 
operating system where each task run part of its task and then go to waiting state. 
When first task is in waiting state, CPU is assigned to second task to complete 
it’s part of task.

Parallelism does not require two tasks to exist. It literally physically run parts of 
tasks OR multiple tasks, at the same time using multi-core infrastructure of CPU, by 
assigning one core to each task or sub-task. Parallelism requires hardware with multiple 
processing units, essentially. In single core CPU, you may get concurrency but NOT 
parallelism.



Futures in Java
---------------

Java docs says, A Future represents the RESULT of an asynchronous computation. 
Methods are provided to check if the computation is complete, to wait for its 
completion, and to retrieve the result of the computation. The result can only 
be retrieved using method get when the computation has completed, blocking if 
necessary until it is ready.

In asynchronous programming, main thread doesn’t wait for any task to finished, 
rather it hand over the task to workers and move on. One way of asynchronous 
processing is using callback methods. Future is another way to write asynchronous 
code. By using Future you can write a method which does long computation but returns 
immediately. Those methods, instead of returning a result, return a Future object. 
You can later get the result by calling Future.get() method, which will return an 
object of type T, where T is what Future object is holding.

Implementing Scrapper Module using Future. The idea here is that you have a various 
sources in a text file. You need to process each sources(URL) where the processing 
involves extracting page-source, fetch the clean content, title and finally convert 
to Result object. The above logic is implemented in invokeCallable method.



Future Limitations
------------------

Future interface provides methods to check if the asynchronous computation is complete 
(using the isDone method), to wait for its completion, and to retrieve its result. But 
these features aren’t enough to let you write concise concurrent code. For example, it’s 
difficult to express dependencies between results of a Future. In order to get result 
from future, we need to call get method which is blocking. What we need is combining  
two asynchronous computations in one—both when they’re independent and when the second 
depends on the result of the first. Reacting to a Future completion (that is, being 
notified when the completion happens and then having the ability to perform a further 
action using the result of the Future , instead of being blocked waiting for its result).
Programmatically completing a Future (that is, by manually providing the result of the 
asynchronous operation).
 


CompletionStage 
---------------

CompletionStage is an interface which abstracts units or blocks of computation 
which may or may not be asynchronous. It is important to realize that multiple 
CompletionStages, or in other words, units of works, can be piped together.

CompletionStage can abstract an asynchronous task and also you can pipe many asynchronous 
outcome in completion stage which lays the foundation of a reactive result processing 
which can have a valid use-case in virtually any area, from Gateways to Clients to 
Enterprise Apps to Cloud Solutions. Furthermore potentially, this reduces superfluous 
polling checks for the availability of result and/or blocking calls on futuristic 
results.


CompletableFuture
-----------------

CompletableFuture is introduced in Java 8 which provides abstraction for async tasks in 
event driven programming. It's designated for executing long running operations (http 
requests, database queries, file operations or complicated computations). 

It can be explicitly completed by calling the complete() method without any synchronous 
wait. It allows values of any type to be available in the future with default return 
values, even if the computation didn’t complete, using default / intermediate results.
With tens of new methods, it also allows you to build a pipeline data process in a 
series of actions. You can find a number of patterns for CompletableFutures such as 
creating a CompletableFuture from a task, or building a CompletableFuture chain. 

 
API description 
---------------

	static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)

		Returns a new CompletableFuture that is asynchronously completed by a task running 
		in the ForkJoinPool.commonPool() with the value obtained by calling the given Supplier.

	static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)

		Returns a new CompletableFuture that is asynchronously completed by a task running 
		in the given executor with the value obtained by calling the given Supplier.


Supplier
--------

java.util.function.Supplier is a functional interface which accepts nothing and supplies 
an output. The supplyAsync() API expects that a result-producing task be wrapped in a 
Supplier instance and handed over to the supplyAsync() method, which would then return 
a CompletableFuture representing this task. This task would, by default, be executed 
with one of the threads from the standard java.util.concurrent.ForkJoinPool 


	public static ForkJoinPool commonPool()

		We can also provide custom thread pool by passing a java.util.concurrent.Executor 
		instance and as such the Supplier tasks would be scheduled on threads from this Executor 
		instance. Similarly we can also supply Runnable instances.

	static CompletableFuture<Void> runAsync(Runnable runnable)

	static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)



Chaining multiple CompletableFutures
------------------------------------

The flexibility of asynchronous task processing actually comes by the virtue of 
chaining multiple tasks in a particular order, such that (asynchronous) completion 
of one CompletableFuture Task might fire asynchronous execution of another separate 
task which helps us to pipeline many asynchronous tasks.

You can see there how we can pipe init, Process A, Process B methods in a asynchronous 
approach. This is just an sample there are plenty more methods where we can use to pipe 
the tasks, also please check then whenComplete method.


Handling Exceptions
-------------------

CompletableFuture API provides the flexibility of handling situations when one 
asynchronous task completes exceptionally. Basically method exceptionally() comes 
handy for this purpose.


Summarizing Completable Future API
----------------------------------

|---------------------------------------------------------------------------------|
|Methods	         |   Takes	          Returns      							  |
|---------------------------------------------------------------------------------|
|thenApply(Async) |	Function	CompletionStage holding the result of the Function|
|---------------------------------------------------------------------------------|
|thenAccept(Async)| 	Consumer	CompletionStage<Void>						  |
|---------------------------------------------------------------------------------|
|thenRun(Async)	 | 	Runnable	CompletionStage<Void>							  |
|---------------------------------------------------------------------------------|



Based on both Stages
--------------------

--------------------------------------------------------------------------------------
Methods	                  Takes	          Returns
--------------------------------------------------------------------------------------
thenCombine(Async)		BiFunction	CompletionStage holding the result of the Function
--------------------------------------------------------------------------------------
thenAcceptBoth(Async)	BiConsumer	CompletionStage<Void>
--------------------------------------------------------------------------------------
runAfterBoth(Async)		Runnable	CompletionStage<Void>
--------------------------------------------------------------------------------------



Based on Either one of the Stages
---------------------------------

--------------------------------------------------------------------------------------
Methods	                  Takes	          Returns
--------------------------------------------------------------------------------------
applyToEither(Async)	Function	CompletionStage holding the result of the Function
--------------------------------------------------------------------------------------
acceptEither(Async)		Consumer	CompletionStage<Void>
--------------------------------------------------------------------------------------
runAfterEither(Async)	Runnable	CompletionStage<Void>
--------------------------------------------------------------------------------------

 
The flexibility of chaining multiple CompletableFutures such that the completion of one 
triggers execution of another CompletableFuture this opens up the paradigm of reactive 
programming in Java. Now there is no blocking call like Future.get() to retrieve the 
result of the future Task.



------------------------------------------------------------------------------------
Future is a placeholder for a result of function that will become available at some 
point in the future. It is asynchronous result and it provides us a way to reference 
something that not available yet. It is read only and could not be changed from outside.
However, futures are rather limited. Probably you can spot the problem here. If you want 
to pass result of callSlowService() operation further, you have to block thread of 
execution with get() method(yep, you are right - it is some sort of polling). Another 
problem is that slow operation could never return. And it could be potential bottleneck. 
Instead of executing operations in parallel we need to wait on result of first operation. 
The CompletableFuture here to help. Here is a simple use case for Future:


	Future<Integer> priceA = callSlowService();
	Future<Integer> priceB = callSlowService();

	try {
	    Future<Integer> afterDiscountA = applyDiscount(priceA.get());
	    Future<Integer> afterDiscountB = applyDiscount(priceB.get());
	    return new FinalPrice().add(afterDiscountA.get()).add(afterDiscountB.get());
	} catch (InterruptedException | ExecutionException ex) {
	    ex.printStackTrace();
	}


CompletableFuture was introduced in Java 8. Also it was available even before 
Java 8 in Guava or Spring Framework as the ListenableFuture. And here just few 
examples what we can do with CompletableFuture. For more info check JavaDocs for 
CompletableFuture and CompletionStage.


	a. Combine several asynchronous operation

	b. Wait for task completion

	c. Listen to Future completion and react to it success or error completion

	d. Chaining results of dependent futures


The getPrice() method query sequentially and then apply discount to it, 

	public static List<PriceRecord> findPricesBlock() {
	    return shops.stream()
	            .map(Shop::getPrice)
	            .map(Discount::applyDiscount)
	            .collect(toList());
	}

By using the parallelStream and this small change improve final result, 

	public static List<PriceRecord> findPricesParallel() {
	    return shops.parallelStream()
	            .map(Shop::getPrice)
	            .map(Discount::applyDiscount)
	            .collect(toList());
	}


Let’s see if we could do better with CompletableFuture. In the next step we create it 
with factory method supplyAsync(…), so our code will be executed in different thread. 

After this we want to apply discount to all prices, also in separate thread and 
thenCompose(…) method do heavy lifting for us. People who are familiar with Scala 
Futures could recognize that this one very similar to flatMap. Also notice we have 
two separate streams here. Becasue of lazy nature of streams all requests to Shop API 
would be executed sequentially if we were using pipeline instead. Most of CompletableFuture 
methods used in this article are non-blocking.

join method here is pretty similar to get() one. The only difference is an exeption it 
could throw is unchecked.


	public static List<PriceRecord> findPricesCF() {

	    List<CompletableFuture<PriceRecord>> futures = shops.stream()
	            .map(s -> CompletableFuture.supplyAsync(s::getPrice))
	            .map(f -> f.thenCompose(p -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(p))))
	            .collect(toList());

	    return futures.stream()
	            .map(CompletableFuture::join)
	            .collect(toList());
	}


Lets run it and… you will be a little disappointed. The execution could be more slower 
comparing with parallel stream one from step Two or could be the same(it is 6055 ms on 
my machine). And here is the reason. Both solutions use theForkJoinPool.commonPool() 
under the hood and use the number of threads equal to number of available processors: 

		Runtime.getRuntime().availableProcesssors(); 

So let’s see if we can do better with custom threadpool.

Here is our custom executor based solution. First we create the thread pool and then 
pass it to CompletableFuture operation as a parameter. And result is 2034 ms. So we 
completed a long way from more than 18000 ms to 2000 ms.


	public static ExecutorService es = Executors.newFixedThreadPool(Math.min(shops.size(), 100), r -> {

	    Thread thread = new Thread(r);
	    thread.setDaemon(true);

	    return thread;
	});


	public static List<PriceRecord> findPricesCustomExecutor() {
		
		List<CompletableFuture<PriceRecord>> futures = shops.stream()
		      .map(s -> CompletableFuture.supplyAsync(s::getPrice, es))
		      .map(f -> f.thenCompose(p -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(p), es)))
		      .collect(toList());

		return futures.stream()
		      .map(CompletableFuture::join)
		      .collect(toList());
	}


Summary
-------

It is the end of examples but not CompletableFuture. It contains the much more useful 
methods inside, for example for asynchronous exeption handling, combining futures 
together, waiting for completion and much more. In this small article I’ve just 
introduced the most common and it’s up to the reader to explore it further. Always use 
overloaded version of get(long, TimeUnit) method of CompletableFuture and provide timeout 
to avoid situation you have blocked client forever.

Try to figure out what you really want, may be simple paralleStream() is just more 
suitable in your case. Remember ThreadPool size formula:

	Nthreads = Ncpu * Ucpu * (1 + W/C)

	where, 

		Ncpu - number of cpus
		Ucpu - cpu utilization
		W/C - ration of wait to compute time	
*/			




class Processor implements Callable<String> {
	
	private int id;
	
	public Processor(int id){
		this.id = id;
	}

	@Override
	public String call() throws Exception {
		Thread.sleep(1000);
		return "Id: "+this.id;
	}
}


public class App {

	
	public static void main(String[] args) {
		
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		List<Future<String>> list = new ArrayList<>();
		
		for(int i=0;i<5;i++){
			Future<String> future = executorService.submit(new Processor(i+1));
			list.add(future);
		}
		
		for(Future<String> future : list){
			try{
				System.out.println(future.get());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		executorService.shutdown();		
	}
}
