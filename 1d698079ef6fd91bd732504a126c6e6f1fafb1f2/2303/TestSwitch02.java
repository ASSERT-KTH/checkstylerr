
/**
 * 测试JDK7中的switch新特性
 * @author dell
 *
 */
public class TestSwitch02 {
	public static void main(String[] args) {
		String  a = "高琪";
		
		switch (a) {   //JDK7的新特性，表达式结果可以是字符串！！！
		case "马士兵":
			System.out.println("输入的马士兵");
			break;
		case "高琪":
			System.out.println("输入的高琪");
			break;
		default:
			System.out.println("大家好！");
			break;
		}
		
	}
}
