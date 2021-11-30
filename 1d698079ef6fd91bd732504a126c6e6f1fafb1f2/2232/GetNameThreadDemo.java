package com.hlj.Some;
public class GetNameThreadDemo extends Thread
{

	public static void main(String[] args)
	{
		
//		GetName();
//		Sleet();

		Interupt();
	}

	private static void Interupt() {
		InteruptRunnable interuptRunnable = new InteruptRunnable();
		Thread thread = new Thread(interuptRunnable, "线程1");

		thread.start();
		try {
			thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("这里的方法是中端");
		thread.interrupt();
		System.out.println("这里的方法是中端");

	}

	private static void Sleet() {
		SleepRunnable sleepRunnable = new SleepRunnable();
		Thread thread = new Thread(sleepRunnable, "线程1");
		Thread thread2 = new Thread(sleepRunnable, "线程2");
		Thread thread3 = new Thread(sleepRunnable, "线程3");
		thread.start();
		thread2.start();
		thread3.start();
		sleepRunnable.run();
	}

	private static void GetName() {
		InterRunnable  nterRunnable = new InterRunnable();
		Thread thread = new Thread(nterRunnable, "线程1");
		Thread thread2 = new Thread(nterRunnable, "线程2");
		Thread thread3 = new Thread(nterRunnable, "线程3");
//测试runnable 线程 
		thread.start();
		thread2.start();
		thread3.start();
//2 .  测试主线程	
		nterRunnable.run();
	}  
}
