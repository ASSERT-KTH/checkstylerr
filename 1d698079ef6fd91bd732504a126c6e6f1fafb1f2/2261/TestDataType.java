//测试整数类型：byte,short,int,long。以及进制之间的转换问题
public class TestDataType {
	
	public static void main(String[] args){
		int a = 10;
		int a2 = 010;
		int a3 = 0xf;
	//	byte b = 200;
	//	System.out.println(b);
		System.out.println(a);
		System.out.println(a2);
		System.out.println(a3);
		System.out.println(Integer.toBinaryString(a));
		System.out.println(Integer.toOctalString(a));
		System.out.println(Integer.toHexString(a));
		
		int a5 = 10;
		long a6 = 200;
		byte b2 = 100;   //如果数据的大小没有超过byte/short/char的表述范围，则可以自动转型。
		
		long  a7 = 11123213232L;
		long l = 3;
		long l2 = l+3;    //L问题
		
	}

}