package com.hlj.Extends.testThreadAndRunnable;


//杩欎釜灏辨槸涓�釜澶氱嚎绋嬬殑鎿嶄綔绫�锛屼笅闈㈠氨璇ュ惎鍔ㄧ嚎绋嬩簡
public class InterRunnable  implements Runnable{
	private int tickets=5;


	//閲嶅啓 run 鏂规硶
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
			while(true)
		  {
				if(tickets>0)
			System.out.println(Thread.currentThread().getName()+"鍑哄敭绁"+tickets--);
		  }
		 
	}
 
}
