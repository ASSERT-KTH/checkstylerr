package myproject01;

import java.util.Scanner;

/**
 * 
 * @author 张宇晋
 * @version 不知道
 *
 */
/*计算 1+2+……n 的值；*/

public class Textwhile {
		public static void main(String args[]){
			System.out.println("计算的 1 + 2 + 3 + …… +n 的值 ");
			System.out.println("       请在下面输入 n 的值");	
			System.out.printf("\n");
				int sum = 0;
			Scanner s = new Scanner(System.in);
			
			int n = s.nextInt(        );
			
			while( n > 0 ){
				sum= sum + n;
				n--;
			}
			System.out.printf("\n");
			System.out.println("计算的 1 + 2 + 3 + …… +n 的值为 ：" + sum);
		}
}
