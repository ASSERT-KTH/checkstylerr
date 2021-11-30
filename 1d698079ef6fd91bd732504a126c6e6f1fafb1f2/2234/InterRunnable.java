package com.hlj.Some;


//这个就是一个多线程的操作类 ，下面就该启动线程了
public class InterRunnable  implements Runnable{


	//重写 run 方法
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		for(int i=0;i<10;i++) //表示循环 10 次
		{
				
			System.out.println(Thread.currentThread().getName()+"线程正在运行");
		 
		}
	}
 
}
