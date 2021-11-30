package cn.bjsxt.stringbuilder;

import java.util.ArrayList;

/**
 * 测试StringBuilder的一些常用方法
 * @author dell
 *
 */
public class Test02 {
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder("abcdefghijklmnopqrstuvwxyz");
		sb.delete(3, 5).delete(3, 5);   //结果：abchijklmnopqrstuvwxyz
		System.out.println(sb);
		sb.reverse();
		System.out.println(sb);
		
		StringBuffer sb2 = new StringBuffer();
	}
}
