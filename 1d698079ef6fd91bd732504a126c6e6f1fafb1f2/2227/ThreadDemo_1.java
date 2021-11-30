package com.hlj.yxj;

public class ThreadDemo_1
{
	public static void main(String args[])
	{
		
		InterRunnable myThred1 = new InterRunnable();

			Thread thread1 = new Thread(myThred1,"线程1");
			Thread thread2 = new Thread(myThred1,"线程2");
			Thread thread3 = new Thread(myThred1,"线程3");

//调用线程体
			thread1.setPriority(Thread.NORM_PRIORITY);
			thread2.setPriority(Thread.MAX_PRIORITY);
			thread3.setPriority(Thread.MIN_PRIORITY);
			
			
			thread1.start(); 
			System.out.println("**************8");
			thread2.start(); 
			thread3.start();  
			System.out.println(Thread.currentThread().getName());
			System.out.println(Thread.MAX_PRIORITY+"max");
			System.out.println(Thread.MIN_PRIORITY+"min");
			System.out.println(Thread.NORM_PRIORITY+"norm");


	}
} 

