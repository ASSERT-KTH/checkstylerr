

/*
 * 测试if语句
 */
public class TestIf {
	public static void main(String[] args) {
		double d = Math.random();
		int e =1+(int)(d*6);  
		System.out.println(e);
		
		
		if(e>3) {
			System.out.println("大数！");
			System.out.println("大数！!!!");
		}else{
			System.out.println("小数！");
		}
			
		
		System.out.println("测试多选择结构");
		if(e==6){
			System.out.println("运气非常好！");
		}else if(e>=4){
			System.out.println("运气不错！");
		}else if(e>=2){
			System.out.println("运气一般吧");
		}else{
			System.out.println("运气不好！");
		}
		
	
	}
}