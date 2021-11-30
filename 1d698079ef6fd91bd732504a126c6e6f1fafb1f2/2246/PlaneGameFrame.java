package cn.bjsxt.plane;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;

import cn.bjsxt.util.Constant;
import cn.bjsxt.util.GameUtil;
import cn.bjsxt.util.MyFrame;

public class PlaneGameFrame extends MyFrame {
	Image bg = GameUtil.getImage("images/bg.jpg");
	Plane p = new Plane("images/plane.png",50,50);
	ArrayList  bulletList = new ArrayList();   //泛型暂时未学，暂不加。以后学了，强烈建议加上。
	
	Date startTime;
	Date endTime;
	
	Explode bao;
	
	public void paint(Graphics g){
		g.drawImage(bg, 0, 0, null);
		p.draw(g);
		for(int i=0;i<bulletList.size();i++){
			Bullet b = (Bullet) bulletList.get(i);
			b.draw(g);
			boolean peng = b.getRect().intersects(p.getRect());
			if(peng){
				p.setLive(false);  //飞机死掉！
				if(bao==null){
					endTime = new Date();
					bao = new Explode(p.x,p.y);
				}
				bao.draw(g);
				
				break;
			}
		}
		
		if(!p.isLive()){
			int period = (int)((endTime.getTime()-startTime.getTime())/1000);
			printInfo(g, "时间："+period+"秒", 20, 120, 260, Color.white);
			
			switch (period/10) {
			case 0:
			case 1:
				printInfo(g, "菜鸟", 50,100,200,Color.white);
				break;
			case 2:
				printInfo(g, "小鸟", 50,100,200,Color.white);
				break;
			case 3:
				printInfo(g, "大鸟", 50,100,200,Color.yellow);
				break;
			case 4:
				printInfo(g, "鸟王子", 50,100,200,Color.yellow);
				break;
			default:
				printInfo(g, "鸟人", 50,100,200,Color.yellow);
				break;
			}
			
			
		}
		
		
//		printInfo(g, "分数：100", 10, 50, 50, Color.yellow);
		
	}
	
	
	/**
	 * 在窗口上打印信息
	 * @param g
	 * @param str
	 * @param size
	 */
	public void printInfo(Graphics g,String str,int size,int x,int y,Color color){
		Color c = g.getColor();
		g.setColor(color);
		Font f = new Font("宋体",Font.BOLD,size);
		g.setFont(f);
		g.drawString(str,x,y);
		g.setColor(c);

	}
	
	
	public static void main(String[] args) {
		new PlaneGameFrame().launchFrame();
	}
	
	
	public void launchFrame(){
		super.launchFrame();
		//增加键盘的监听
		addKeyListener(new KeyMonitor());
		
		//生成一堆子弹
		for(int i=0;i<35;i++){
			Bullet b = new Bullet();
			bulletList.add(b);
		}
		
		startTime = new Date();
	}
	
	
	//定义为内部类，可以方便的使用外部类的普通属性
	class KeyMonitor extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
//			System.out.println("按下："+e.getKeyCode());
			p.addDirection(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			p.minusDirection(e);
		}
		
	}
	
	
	
}
