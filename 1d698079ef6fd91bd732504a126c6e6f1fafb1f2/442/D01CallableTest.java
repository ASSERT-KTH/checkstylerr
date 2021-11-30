package com.hlj.moudle.thread.d12Callable;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/** 
 * @author : HealerJean
 * @date   ： 2017年12月20日 下午4:58:19
 *
 * 1、两者最大的不同点是：实现Callable接口的任务线程能返回执行结果；而实现Runnable接口的任务线程不能返回结果；
 * 2、Callable接口的call()方法允许抛出异常；而Runnable接口的run()方法的异常只能在内部消化，不能继续上抛；
 * 3、行Callable任务可以拿到一个Future对象，Future 表示异步计算的结果。它提供了检查计算是否完成的方法，以等待计算的完成，并获取计算的结果。
 *
 * 计算完成后只能使用 get 方法来获取结果，如果线程没有执行完，Future.get()方法可能会阻塞当前线程的执行
 *
 */

public class D01CallableTest {

  /**
   * 
   	1、调用线程的第一种方法
   */ 
	@Test
	public void testStart() throws InterruptedException, ExecutionException{
		
	        Callable<String> callable = new CallableTaskWithResult(45);

			//这个既是线程又是相当于是一个任务的返回
	        FutureTask<String> task = new FutureTask<String>(callable);
	        
	        new Thread(task).start();
	        
	        String result = task.get(); 
	        
	        System.out.println(result); 
	}
	

	
	

class CallableTaskWithResult implements Callable<String> {  
    private int id;  
    public CallableTaskWithResult(int id) {  
        this.id=id;  
    }
    
	@Override
	public String call() throws Exception {
		System.out.println(Thread.currentThread().getName()); 
		return "id为："+id;
	}  
}

}