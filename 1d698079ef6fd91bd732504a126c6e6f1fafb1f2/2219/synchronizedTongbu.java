package com.hlj.tongbu;


 class InterRunnable implements Runnable
{	private int tickets=5;


		//閲嶅啓 run 鏂规硶
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
				sale();
			 
		}


		private synchronized void sale() {
			while(true)
  {  
				
				if(this.tickets>0){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			System.out.println(Thread.currentThread().getName()+"鍑哄敭绁"+this.tickets--);
					}
				
  }
		}
}

 public class synchronizedTongbu{

	public static void main(String args[])
	{

		InterRunnable interRunnable = new InterRunnable();
		
		Thread thread = new Thread(interRunnable,"绁�");
		Thread thread2 = new Thread(interRunnable,"绁�");

		thread.start();
		thread2.start();
	}
 
}
