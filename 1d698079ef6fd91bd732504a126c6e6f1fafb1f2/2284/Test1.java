package cn.bjsxt.oop;

public class Test1 {
	 public static void main(String[] args) {
		 //通过类加载器Class Loader加载Student类。 加载后，在方法区中就有了Student类的信息！
		Student s1 = new Student();
		
		s1.name = "高琪";
		s1.study();
		s1.sayHello("馬士兵");
		
		Student s2 = new Student();
		s2.age = 18;
		s2.name="老高";
		
	}
}
