package cn.bjsxt.oop;

public class Test2 {
	public static void main(String[] args) {
		Student s1 = new Student();
		s1.name = "¸ßç÷";
		s1.age = 18;
		
		Computer c = new Computer();
		c.brand = "ÁªÏë";
		c.cpuSpeed = 100;
		
		s1.computer = c;
		
		c.brand = "´÷¶û";
		
		System.out.println(s1.computer.brand);
	}
}
