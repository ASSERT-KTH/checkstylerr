package com.hlj.Extends.testThreadAndRunnable;

public class ThreadDemo_1
{
	public static void main(String args[])
	{
	/*	
		ExtendsThread myThred1 = new ExtendsThread();
		ExtendsThread myThred2 = new ExtendsThread();

		myThred1.start();
		System.out.println("**************8");
		myThred2.start(); 
		*/
		InterRunnable interRunnable = new InterRunnable();
		
		Thread thread = new Thread(interRunnable);
		Thread thread2 = new Thread(interRunnable);

		thread.start();
		thread2.start();
	}
} 

