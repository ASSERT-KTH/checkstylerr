package com.hlj.ss;


 class Fang
{	
	 public synchronized void say( Zhang zhang) {
			System.out.println(" 那钱来");
			zhang.gave();
	  }
	public synchronized void gave() {
			
			System.out.println(" 钱到手了");
	  }
}
class Zhang{
	public synchronized void say(Fang fang) {
			
			System.out.println(" 我给你钱");
			fang.gave();
	  }
	public synchronized void gave() {
			
			System.out.println(" 弟弟回来");
	  }	
}
 public class Sisuo implements Runnable{
	 Fang fang = new Fang();
	 Zhang zhang = new Zhang();
	public static void main(String args[])
	{
			Sisuo sisuo = new Sisuo();	
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		fang.say(zhang); 
	
	}	
		 public Sisuo(){
		
			 new Thread(this).start();
			 zhang.say(fang);			
		 }		 		
}
