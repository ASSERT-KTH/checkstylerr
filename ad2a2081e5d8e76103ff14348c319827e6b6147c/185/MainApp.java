package cn.itcast.demo8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
/*
 * ��֪ ����һ���ಢ��֪����·�� ��cn.itcast.demo8.User
 *  
 *  ���ݸ�·�� ��ȡ��  User����ֽ����ļ�����
 *  �����ֽ������ ��ȡ �� �� �ղι���
 *  ���ݿղι����newInstance���� ͨ�����䷽ʽ��������
 *  
 *  �ڸ��ݷ����ȡ��
 */
public class MainApp {
	public static void main(String[] args) throws Exception {
		 //��ȡ��  User����ֽ����ļ�����
		Class clazz = Class.forName("cn.itcast.demo8.User");
	   //�����ֽ����ļ������ȡ�ղι���
		Constructor cons = clazz.getConstructor();
		//���ݷ����ȡ����
		Object obj = cons.newInstance();
		
		//���ݷ����ȡ�÷��� 
		Method method = clazz.getMethod("show");
		
		method.invoke(obj);
	}
}
