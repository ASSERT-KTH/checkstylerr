package cn.bjsxt.oop.testFinal;


public /*final*/ class Animal {    //final修饰类则说明，这个类不能被继承！
	
	public /*final*/ void run(){   //final加到方法前面，意味着该方法不能被子类重写！
		System.out.println("跑跑！");
	}

}


class Bird  extends Animal {
	
	public void run(){
		super.run();
		System.out.println("我是一个小小小小鸟，飞呀飞不高");
	}
	
}

