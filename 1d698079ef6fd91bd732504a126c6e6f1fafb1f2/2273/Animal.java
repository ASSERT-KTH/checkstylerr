package cn.bjsxt.oop.inherit;

/**
 * 测试继承
 * @author dell
 *
 */
public class Animal /*extends Object*/ {
	String eye;
	
	public void run(){
		System.out.println("跑跑！");
	}
	public void eat(){
		System.out.println("吃吃！");
	}
	public void sleep(){
		System.out.println("zzzzz");
	}
	
	public  Animal(){
		super();
		System.out.println("创建一个动物！");
	}
	
}

class Mammal extends Animal {
	
	public void taisheng(){
		System.out.println("我是胎生");
	}
	
}

class Bird  extends Animal {
	public void run(){
		super.run();
		System.out.println("我是一个小小小小鸟，飞呀飞不高");
	}
	
	public void eggSheng(){
		System.out.println("卵生");
	}
	
	public Bird(){
		super();
		System.out.println("建一个鸟对象");
	}
	
}

