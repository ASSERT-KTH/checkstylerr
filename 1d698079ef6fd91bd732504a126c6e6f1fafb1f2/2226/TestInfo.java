package com.hlj.zuihou;

public class TestInfo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Info info = new Info();
		costumer costumer1 = new costumer(info);
		Product product = new Product(info);
		new Thread(product).start();
		new Thread(costumer1).start();

	}

}
