package com.hlj.zuihou;

public class Product implements Runnable{
	Info info = null;
	@Override
	public void run() {
		// TODO Auto-generated method stub
//不断的生产
		for (int i = 0; i < 50; i++) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (i%2==1) {
				this.info.set("www.baidiu.com"+i, "zhang"+i);
//				info.setContent("www.baidiu.com"+i);
//				info.setName("zhang"+i);
			}else {
				this.info.set("www.sohu.com"+i, "healer"+i);

//				info.setContent("www.sohu.com"+i);
//				info.setName("healer"+i);
			}
		}
	}
	public Product(Info info)
	{
		this.info = info;
	}
}
