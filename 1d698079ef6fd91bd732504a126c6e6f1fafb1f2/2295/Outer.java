package cn.bjsxt.innerClass;


/**
 * 测试内部类的使用
 * @author dell
 *
 */
public class Outer {
	public static void main(String[] args) {
		Face f = new Face();
		Face.Nose n = f.new Nose();
		n.breath();
		Face.Ear e = new Face.Ear();
		e.listen();
	}
}

class Face {
	int type;
	String shape="瓜子脸";
	static String color="红润";
	
	class Nose {
		
		void breath(){
			System.out.println(shape); 
			System.out.println(Face.this.type);
			System.out.println("呼吸！");
		}
	}
	
	static class Ear {
		void listen(){
			System.out.println(color); 
			System.out.println("我在听！"); 
		}
	}
	
}