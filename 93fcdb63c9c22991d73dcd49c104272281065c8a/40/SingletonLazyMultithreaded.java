
public class SingletonLazyMultithreaded {

	private static SingletonLazyMultithreaded sc = null;

	private SingletonLazyMultithreaded(){}
	
	public static synchronized SingletonLazyMultithreaded getInstance(){
		if(sc==null){
			sc = new SingletonLazyMultithreaded();
		}
		return sc;
	}
}