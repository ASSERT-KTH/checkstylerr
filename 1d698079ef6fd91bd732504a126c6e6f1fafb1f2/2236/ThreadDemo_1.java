package com.hlj.InterRunnable;

public class ThreadDemo_1
{
	public static void main(String args[])
	{
		
		MyThred myThred1 = new MyThred("zhang");

			Thread thread1 = new Thread(myThred1);
			Thread thread2 = new Thread(myThred1);
//调用线程体
			thread1.start();
			System.out.println("**************8");
			thread2.start(); 
		
	}
} 

