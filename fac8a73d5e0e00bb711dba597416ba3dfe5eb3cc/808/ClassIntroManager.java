package org.onetwo.common.reflect;

import java.util.Map;
import java.util.WeakHashMap;

import org.onetwo.common.utils.StringUtils;

public class ClassIntroManager {

	private static final ClassIntroManager introManager = new ClassIntroManager();
	
	public static ClassIntroManager getInstance() {
		return introManager;
	}
	
	private Map<Class<?>, Intro<?>> introMaps = new WeakHashMap<Class<?>, Intro<?>>(500);
	private Object lock = new Object();

	@SuppressWarnings("unchecked")
	public <T> Intro<T> getIntro(Class<T> clazz){
		if(clazz==null)
			return null;
		Intro<T> intro = (Intro<T>)introMaps.get(clazz);
		if(intro==null){
			synchronized (lock) {
				intro = (Intro<T>)introMaps.get(clazz);
				if(intro==null){
					intro = Intro.wrap(clazz);
					introMaps.put(clazz, intro);
				}
			}
		}
		return intro;
	}

	public Intro<?> getIntro(String className){
		if(StringUtils.isBlank(className))
			return null;
		Class<?> clazz = ReflectUtils.loadClass(className);
		return getIntro(clazz);
	}

	
}
