package myproject01;
/**
 * 
 * @author 张宇晋
 * @version 不知道
 *
 */
public class Textswitch {
		public static void main(String args[]){
			double b = Math.random();
			
			int c = (int)(b * 6) +1;
			
			System.out.printf("\n");
			System.out.println(" 筛子数为 1 2 3 ，则是 ‘主’ 方赢");
			
			System.out.printf("\n");
			System.out.println(" 筛子数为 4 5 6 ，则是 ‘客’ 方赢");
			
			System.out.printf("\n");			
			System.out.println("     筛子数为： "+c);	
			
			System.out.printf("\n");
			switch (c){
			
			case       1:
			case	   2:
			case	   3:
			System.out.println("     主   赢，    客方手气不佳");
				   break;
			default:
			System.out.println("     客   赢，     主方手气不佳");
			break;
			}
		}
}
