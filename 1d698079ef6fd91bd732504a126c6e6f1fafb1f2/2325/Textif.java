package myproject01;


/**
 * 
 * @author 张宇晋
 * @version 7.0
 *
 */
public class Textif {
	public static void main(String args[]){
		double a = Math.random();  //这时一个[0,1)之间的随机数

		int b = (int)(a * 6) + 1;  //设置一个置筛子游戏
		
			System.out.printf("\n");
			System.out.println(" 筛子数为 1 2 3 ，则是 ‘主’ 方赢");
			
			System.out.printf("\n");
			System.out.println(" 筛子数为 4 5 6 ，则是 ‘客’ 方赢");
			
			System.out.printf("\n");			
			System.out.println("     筛子数为： "+b);	
			
			System.out.printf("\n");
		if ( b < 4)
		{
	
			System.out.println("     主   赢，    客方手气不佳");
			
		}
		else{
		
			System.out.println("     客   赢，主方手气不佳");			
		}
		


	}

}
