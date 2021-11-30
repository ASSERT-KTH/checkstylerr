
/**
 * 测试while循环的用法
 * @author dell
 *
 */
public class TestWhile {
	public static void main(String[] args) {
		
		int a = 1;    //初始化
		while(a<=100){  //条件判断
			System.out.println(a);    //循环体
			a++;  //迭代
		}
		System.out.println("while循环结束！");
		
		//计算：1+2+3+...+100
		int b = 1;
		int sum = 0;
		while(b<=100){
			sum += b;  //sum = sum + b;
			b++;
		}
		System.out.println("和为："+sum); 
		
	}
}
