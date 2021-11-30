package 第一个试用包;

import java.util.Scanner;

/**
 * 本程序是在测试制作API文件(计算a + b 的和)
 * @author Lenovo
 * @version 1.8
 */
public class ZhizuoAPI {
	public static void main(String args[]){
		int a,b,c;
		Scanner s = new Scanner(System.in);
		System.out.printf("   请输入 a 的值：\t");
		a = s.nextInt();
		System.out.printf("\n");
		
		System.out.printf("   请输入 b 的值：\t");
		b = s.nextInt();
		
		
		c = jiafa(a,b);
		System.out.printf("         a + b = ");
		System.out.printf(" "+c);
	}

	/**
	 * 制作一个加法的方法（或者叫函数）
	 * @param a  一个加数
	 * @param b 一个被加数
	 * @return  a和b 的和
	 */
	//上面的东西写在方发的前面
	public static int jiafa(int a,int b){
		int and;
		and = a + b;
		 return and;
	}
}

