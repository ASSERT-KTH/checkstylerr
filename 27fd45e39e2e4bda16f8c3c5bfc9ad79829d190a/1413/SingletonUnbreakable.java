

/*
Options to break the Singleton class 
------------------------------------

If the class is in the the multi-treaded environment 

If the class is Serializable.

If it’s Clonable.

It can be break by Reflection.

If the class is loaded by multiple class loaders
*/

import java.io.ObjectStreamException;
import java.io.Serializable;


public class SingletonUnbreakable implements Serializable{


	private static final long serialVersionUID = 1L;
	
	private static SingletonUnbreakable sc = new Singleton();
	// private static SingletonUnbreakable sc = null;

	private SingletonUnbreakable(){
		if(sc!=null){
			throw new IllegalStateException("Already created.");
		}
	}

	// public static SingletonLazy getInstance(){
	// 	if(sc==null){
	// 		sc = new SingletonLazy();
	// 	}
	// 	return sc;
	// }

	public static SingletonUnbreakable getInstance(){
		return sc;
	}
	
	private Object readResolve() throws ObjectStreamException{
		return sc;
	}
	
	private Object writeReplace() throws ObjectStreamException{
		return sc;
	}
	
	public Object clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException("Singleton, cannot be clonned");
	}
	
	private static Class getClass(String classname) throws ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(classLoader == null) 
			classLoader = Singleton.class.getClassLoader();
		return (classLoader.loadClass(classname));
	}
	
}