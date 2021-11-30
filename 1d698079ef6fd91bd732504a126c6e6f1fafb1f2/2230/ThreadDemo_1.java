package com.hlj.ExtendsThread;

public class ThreadDemo_1
{
	public static void main(String args[])
	{
		
		MyThred myThred1 = new MyThred("zhang");
		MyThred myThred2 = new MyThred("gao");
		//线程开始 执行 run这个方法，java虚拟机 将会自动 运行run方法

	/*	myThred1.run();
		System.out.println("**************8");

		myThred2.run();*/
		
//调用线程体
		myThred1.start();
		System.out.println("**************8");
		myThred2.start(); 
		
	}
} 

