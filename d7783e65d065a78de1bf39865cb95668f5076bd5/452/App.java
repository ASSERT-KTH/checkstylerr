package com.balazsholczer.udemy;


public class App {


	private static int counter = 0;

	public static synchronized void increment() {

		// counter = counter + 1;
		++counter;
	}
	
	public static void process() {

		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {

				for (int i = 0; i < 100; ++i){
					increment();
				}
			}
		});

		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {

				for (int i = 0; i < 100; ++i)

					increment();

					/*
					// this is not an atomic operation 
					// It has 3 steps: read, increment and write
					// the threads will have conlflicts and wont work as expected 
				    counter = counter + 1;
					*/
			}
		});
		
		t1.start();
		t2.start();
		
		try {

			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {

		process();
		System.out.println(counter);	
	}
}




