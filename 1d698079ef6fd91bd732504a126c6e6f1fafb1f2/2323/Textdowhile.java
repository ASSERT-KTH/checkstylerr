package myproject01;

import java.util.Scanner;

public class Textdowhile {
		public static void main(String args[]){
			System.out.println("计算的 1 + 2 + 3 + …… +n 的值 ");
			System.out.println("       请在下面输入 n 的值");	
			System.out.printf("\n");
	Scanner s = new Scanner(System.in);
	int sum = 0,n=s.nextInt();
		do{
			sum = sum + n;
			n--;
		}while( n >0);		
		System.out.println("     计算的 1 + 2 + 3 + …… +n 的值为：" + sum);	
		}

}
