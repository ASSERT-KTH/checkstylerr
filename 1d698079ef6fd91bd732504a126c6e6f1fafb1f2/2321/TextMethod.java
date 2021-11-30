package myproject01;

import java.io.InputStream;
import java.util.Scanner;

/**
 * 
 * @author 张宇晋
 *
 */
/*
 * 计算两个数的加法
 */
public class TextMethod {
	public static void main(String args[]){
		text01();
		int a,b,c;
		Scanner s = new Scanner(System.in); 
		
		System.out.printf("\n");		
		System.out.println("    输入第一个数：");
		System.out.printf("\t");
		a = s.nextInt();
		
		System.out.printf("\n");		
		System.out.println("    输入第二个数：");
		System.out.printf("\t");
		b = s.nextInt();
		
		System.out.printf("\n");
		c = max(a,b);
		System.out.printf("\t"+c);
	}
	private static Scanner Scanner(InputStream in) {
		// TODO 自动生成的方法存根
		return null;
	}
	public static void text01(){
		
		System.out.println("  输入两个数的值，并且计算二者的和 ");
	}
	public static int max(int a,int b){
		int jia = a + b;
		return jia;
	}
}
