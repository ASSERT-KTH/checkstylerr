package cn.itcast.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
/*
 * ����һ����׼��Person�࣬��������name������setget����,�����ղι��졣
     ʹ�÷���ķ�ʽ����һ��ʵ��������ʼ����
      ʹ�÷��䷽ʽ����setName���������ƽ�������,
      ����֮�� ��ͨ�����䷽ʽִ��getName()����
 */
public class MainApp {
	public static void main(String[] args) throws Exception {
		
		
		
		 //��ȡ��  Person����ֽ����ļ�����
		Class clazz = Class.forName("cn.itcast.demo.Person");
	   //�����ֽ����ļ������ȡ�ղι���
		//���ݷ����ȡ����
		Object obj = clazz.newInstance();
		
		//����setName���������ƽ������� 
		Method method = clazz.getMethod("setName",String.class);
		
		method.invoke(obj,"С��");
		
		//ͨ�����䷽ʽִ��getName()����
		Method method2 = clazz.getMethod("getName");
		System.out.println(method2.invoke(obj));
		
	}
}
