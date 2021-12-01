package com.atguigu.java11;

import static org.junit.Assert.*;

import java.io.FileOutputStream;

import org.junit.Test;

public class InputStreamTest {
	
	@Test
	public void testName() throws Exception {
		var cl = this.getClass().getClassLoader();
		try (var is = cl.getResourceAsStream("file"); var os = new FileOutputStream("file2")) {
			is.transferTo(os); // ���������е���������ֱ���Զ��ظ��Ƶ��������
		}
	}
}
