
public class TestOperator {
	
	public static void main(String[] args){
	/*	
		double d = 10.2%3;
		System.out.println(d);
		
		int a = 3;
		int b = a++;   //执行完后,b=3。先给b赋值，再自增。
		int c = ++a;   //执行完后,c=5。先自增,再给b赋值
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
	*/	
	//	int c = 3/0;
		
	/*	
		boolean c = 1<2&&2>(3/0); 
		System.out.println(c);
	*/
	/*
		//测试位运算
		int m = 8;
		int n = 4;
		System.out.println(m&n);
		System.out.println(m|n);
		System.out.println(~m);
		System.out.println(m^n);
		
		
        int a = 3*2*2;
		int b = 3<<3;  //相当于：3*2*2;
		int c = 12/2/2;
		int d = 12>>2;
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		
		boolean b1 = true&false;
		System.out.println(b1);
	*/	
	
	/*
		//扩展运算符
		int  a = 3;
		a +=5;  //a = a+5;
	*/	
	
	/*
		//字符串相连：加号两边只要有一个为字符串，则变为字符串连接符，整个结果为字符串。
		System.out.println(4+"5");
	*/	
	
		int a=3;
		int b=5;
		String str= "";
		/*
		if(a<b){
			str = "a<b";
		}else{
			str = "a>b";
		}
		*/
		str = (a<b)?"a<b":"a>=b";
		System.out.println(str);

		
	}

}