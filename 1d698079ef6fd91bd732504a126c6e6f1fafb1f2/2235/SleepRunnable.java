package com.hlj.Some;


//这个就是一个多线程的操作类 ，下面就该启动线程了
public class SleepRunnable  implements Runnable{

	public void run() {
		// TODO Auto-generated method stub
		
		for(int i=0;i<10;i++) //表示循环 10 次
		{
				try {					
//每个休眠500毫秒
					Thread.sleep(500);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			System.out.println(Thread.currentThread().getName()+"线程正在运行");
		 
		}
	}
 
}
