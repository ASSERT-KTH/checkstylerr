package cn.bjsxt.oop.encapsulation01;

public class Test01 {
	private String str;
	protected void print(){
		String s = str;
		System.out.println("Test01.print()");
	}
}

class Test001 extends Test01 {
	public void pp(){
		super.print();
	}
	public static void main(String[] args) {
		Test01 t = new Test01();
		t.print();
	}
}
