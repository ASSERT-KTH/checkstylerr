package com.hlj.InterRunnable;


//这个就是一个多线程的操作类 ，下面就该启动线程了
public class MyThred  implements Runnable{

	String name;
	public MyThred(String name) {
		super();
		this.name = name;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int i=0;i<10;i++) //表示循环 10 次
		{
			System.out.println("name:"+name+i);
		}
	}

 
}
