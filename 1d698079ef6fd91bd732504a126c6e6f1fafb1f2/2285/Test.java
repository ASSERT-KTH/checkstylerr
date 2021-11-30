package cn.bjsxt.oop.polymorphism;

public class Test {
	
	public static void testAnimalVoice(Animal c){
		c.voice();
		if(c instanceof Cat){
			((Cat) c).catchMouse();
		}
	}
	
	/*
	public static void testAnimalVoice(Dog c){
		c.voice();
	}
	
	public static void testAnimalVoice(Pig c){
		c.voice();
	}*/
//javac Test.java	
//	java Test
	public static void main(String[] args) {
		Animal a = new Cat();
		Cat a2 = (Cat)a;
		testAnimalVoice(a);

//		a2.catchMouse();

//		Animal b = new Dog();
//		Animal c = new Tiger();
//		testAnimalVoice(b);
//		testAnimalVoice(c);
		
	}
}
