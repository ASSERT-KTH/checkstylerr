
/*
 * 测试swtich语句
 */
public class TestSwitch {
	public static void main(String[] args) {
		double d = Math.random();
		int e =1+(int)(d*6);  
		System.out.println(e);
		
		System.out.println("测试多选择结构");
		if(e==6){
			System.out.println("运气非常好！");
		}else if(e==5){
			System.out.println("运气很不错！");
		}else if(e==4){
			System.out.println("运气还行吧");
		}else{   //1,2,3
			System.out.println("运气不好！");
		}
		
		System.out.println("#########################");
		switch(e){   //int,或者自动可以转为int的类型(byte,char,short)。枚举。//JDK7中可以放置字符串。
		case 6:
			System.out.println("运气非常好！");
			break;    //一般在每个case后面都要加break，防止出现case穿透现象。
		case 5:
			System.out.println("运气很不错！");
			break;
		case 4:
			System.out.println("运气还行吧");
			break;
		default:
			System.out.println("运气不好！");
			break;
		}
		
		System.out.println("***************************下面例子反过来利用了case穿透现象！");
		char  c = 'a';
		int rand =(int) (26*Math.random());
		char c2 = (char)(c+rand);
		System.out.print(c2 + ": ");
		switch (c2) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			System.out.println("元音");  
			break;
		case 'y':
		case 'w':
			System.out.println("半元音");   
			break;
		default:
			System.out.println("辅音");
		}
		
		
		
	}
}
