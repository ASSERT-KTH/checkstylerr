
/**
 * while和for循环后的练习题目
 * @author dell
 *
 */
public class TestWhileFor {
	public static void main(String[] args) {
		int oddSum = 0;  //用来保存奇数的和
		int evenSum = 0;  //用来存放偶数的和
		for(int i=0;i<=100;i++){
			if(i%2!=0){
				oddSum += i;
			}else{
				evenSum += i;
			}
			
		}
		System.out.println("奇数的和："+oddSum); 
		System.out.println("偶数的和："+evenSum); 
		
		System.out.println("#########################"); 
		
		for(int j = 1;j<=1000;j++){
			if(j%5==0){
				System.out.print(j+"\t");
			}
			if(j%(5*3)==0){
				System.out.println();
			}
		}
		
	}
}
