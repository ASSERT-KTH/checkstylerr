package com.hlj.zuihou;

public class costumer implements Runnable{
	Info info = null;

	public costumer(Info info) {
		super();
		this.info = info;
	}
//不断的取取内容
	@Override 
	public void run() {
		// TODO Auto-generated method stub
		for (int i = 0; i < 50; i++) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println(this.info.getContent()+this.info.getName()); 
			this.info.get();
		}
	} 
	
}
