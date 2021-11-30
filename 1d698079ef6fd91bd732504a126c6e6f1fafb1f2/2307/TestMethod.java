/**
 * 测试方法
 * 
 * @author dell
 * 
 */
public class TestMethod {
	
	//设计方法的原则：方法的本意是功能块，就是实现某个功能的语句块的集合。   我们设计方法的时候，最好保持方法的原子性，就是一个方法只完成1个功能，这样利于我们后期的扩展。
	public static void test01(int a) {
		int oddSum = 0; // 用来保存奇数的和
		int evenSum = 0; // 用来存放偶数的和
		for (int i = 0; i <= a; i++) {
			if (i % 2 != 0) {
				oddSum += i;
			} else {
				evenSum += i;
			}

		}
		System.out.println("奇数的和：" + oddSum);
		System.out.println("偶数的和：" + evenSum);
	}
	
	public static void test02(int a,int b,int c){
		for (int j = 1; j <= a; j++) {
			if (j % b == 0) {
				System.out.print(j + "\t");
			}
			if (j % (b * c) == 0) {
				System.out.println();
			}
		}
	}
	
	public static int add(int a,int b){
		int sum = a+b;
		if(a==3){
			return 0;    //return 两个作用：结束方法的运行、返回值。
		}
		System.out.println("输出");
		return sum;
	}
	
	public static void main(String[] args) {
		test01(1000);
		test02(100,6,3);  //1-100之间，可以被6整除，每行输出3个。
		System.out.println("###########");
		int s = add(3,5);
		System.out.println(s);

	}
}
