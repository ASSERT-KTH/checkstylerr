import java.util.Scanner;
/*
��֧�ṹ��
	����֧�ṹ��
		ֻ����һ�������жϣ�������ϣ���ĳЩ����
	˫��֧�ṹ��
		���������жϵ�ʱ��ֻ������ѡ��
	���֧�ṹ��
		���Խ��ж���������жϣ�ÿ��ƥ�������ѡ��ͬ��ִ�н��
	Ƕ�׷�֧�ṹ��
		�ڷ�֧�ṹ��Ƕ�׷�֧�ṹ
	switch���֧�ṹ��
		һ��������ֵ�ж�
*/
public class IfDemo{
	
	public static void main(String[] args){
		
		//����֧�ж�,Math.random()�������ݵķ�Χ��[0,1)
		//�õ�0-5֮��������
		//int i = (int)(Math.random()*6);
		//if(i>3){
		//	System.out.println("ֵ����3");
		//}
		//System.out.println("number:"+i);
		
		/*
		double i = 6 * Math.random();
		double j = 6 * Math.random();
		double k = 6 * Math.random();
		int count = (int) (i + j + k);
		if(count > 15) {
			System.out.println("������������");
		}
		if(count >= 10 && count <= 15) {   //����д����10<count<15
			System.out.println("����������һ��");
		}
		if(count < 10) {
			System.out.println("������������ô��");
		}
		System.out.println("����" + count + "��");*/
		
		
		//˫��֧�ṹ
		/*
		int r = 1;
		double PI = 3.14;
		double area = PI * r * r;
		double length = 2 * PI * r;
		if(area >= length){
			System.out.println("������ڵ����ܳ�");
		}else{
			System.out.println("�ܳ��������");
		}
		*/
		//Scanner
		//�����ļ�ɨ��������System.in��ʾ���Ǳ�׼���룬���Դӿ���̨��ȡ����(װ����ģʽ)
		//ע��:ÿ�ζ�ȡ������ֵ�����ַ������ͣ���Ҫ��������ת��
		//Scanner sc = new Scanner(System.in);
		//System.out.println("����������");
		//String str = sc.nextLine();
		//System.out.println(str);
		
		
		//���֧�ṹ
		/*
		int age = (int)(Math.random()*100);
		if(age<10){
			System.out.println("��ͯ");
		}else if(age<20){
			System.out.println("����");
		}else if(age<30){
			System.out.println("����");
		}else if(age<50){
			System.out.println("����");
		}else if(age<70){
			System.out.println("����");
		}else{
			System.out.println("���");
		}
		*/
		
		//Ƕ�׷�֧�ṹ
		/*
		int time = (int)(Math.random()*40);
		if(time<20){
			System.out.println("��ϲ�������");
			String sex = ((int)(Math.random()*2))==0?"girl":"boy";
			if(sex=="girl"){
				System.out.println("��ӭ����Ů����");
			}else{
				System.out.println("��ӭ����������");
			}
		}else{
			System.out.println("�ɼ�̫�����̭");
		}
		*/
		
		//switch���֧�ṹ
		/*ע�⣺
			1��ÿ��caseģ����Ҫ���break����ֹ���ƥ��
			2��������case�д�����߼������Ĺ���һ�£����Կ���ֻ��������һ�δ���
			3��default��ʾĬ��ѡ������е�case��ƥ���ʱ�򣬻�ִ�д�ѡ��
			4��defult�����У�Ҳ����û��
		*/
		int random = (int)(Math.random()*26);
		char ch = (char)('a'+random);
		switch(ch){
			/*
			case 'a':
			System.out.println("Ԫ����"+ch);
			break;
			case 'e':
			System.out.println("Ԫ����"+ch);
			break;
			case 'i':
			System.out.println("Ԫ����"+ch);
			break;
			case 'o':
			System.out.println("Ԫ����"+ch);
			break;
			case 'u':
			System.out.println("Ԫ����"+ch);
			break;
			case 'y':
			System.out.println("��Ԫ����"+ch);
			break;
			case 'w':
			System.out.println("��Ԫ����"+ch);
			break;
			default:
			System.out.println("������"+ch);*/
			case 'a':
			case 'e':
			case 'i':	
			case 'o':
			case 'u':
			System.out.println("Ԫ����"+ch);
			break;
			case 'y':
			case 'w':
			System.out.println("��Ԫ����"+ch);
			break;
			default:
			System.out.println("������"+ch);
		}
		
		
		
		
		
		
		
		
		
		
		
	}
}