//测试浮点数
public class TestFloatType {
	
	public static void main(String[] args){
	
		//double d = 3.14;   //浮点数常量默认类型是double。
		//float f = 6.28F;
		double d2 = 314e-2;   //采用科学计数法的写法
		System.out.println(d2);
		
		float f = 0.1f;
		double d = 1.0/10;
		System.out.println(f==d);   //false
	
		
	}

}