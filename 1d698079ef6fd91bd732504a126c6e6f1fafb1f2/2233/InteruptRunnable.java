package com.hlj.Some;

public class InteruptRunnable implements Runnable{


	public void run() {
		// TODO Auto-generated method stub
		System.out.println(" 1. 进入线程"); 
		
				try {					
//每个休眠500毫秒//
					System.out.println("2. 线程将要 休眠");
					Thread.sleep(500);
					 System.out.println("3. 线程正在休眠");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				
					System.out.println("4. 线程中断休眠");
						return; //一，当线程休眠中断之后就会发生异常  旦有了异常,返回方法调用处
				}
			System.out.println(Thread.currentThread().getName()+"5. 线程正在运行");
		 
		
	}
 
}
