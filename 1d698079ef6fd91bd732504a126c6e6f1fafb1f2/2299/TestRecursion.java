/**
 * ²âÊÔµİ¹éËã·¨
 *
 */
public class TestRecursion {
	static int a = 0;
	
	public static void test01(){
		a++;
		System.out.println("test01:"+a);
		if(a<=10){  //µİ¹éÍ·
			test01();
		}else{      //µİ¹éÌå
			System.out.println("over");
		}
	}
	
	public static void test02(){
		System.out.println("TestRecursion.test02()");
	}
	
	
	public static long factorial(int n){
		if(n==1){
			return 1;
		}else{
			return n*factorial(n-1);
		}
	}
	
	public static void main(String[] args) {
		test01();
		System.out.println(factorial(10));  
	}
}
