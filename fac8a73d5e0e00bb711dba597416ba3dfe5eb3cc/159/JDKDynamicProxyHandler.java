package org.onetwo.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.ClassUtils;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

abstract public class JDKDynamicProxyHandler implements InvocationHandler {
	
	public static final String METHOD_TO_STRING = "toString";

	final protected Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	final private Object proxyObject;
	private List<String> excludeMethods = ImmutableList.of("equals", "hashCode", "clone");
	private List<Class<?>> proxyInterfaces;
	
	public JDKDynamicProxyHandler(Class<?>... proxiedInterfaces){
		/*if(excludeTargetClass!=null){
			excludeDeclaredMethod(excludeTargetClass);
		}*/
		this.proxyObject = Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), proxiedInterfaces, this);
		this.proxyInterfaces = Arrays.asList(proxiedInterfaces);
	}
	
	final protected void excludeDeclaredMethod(Class<?> targetClass){
		Method[] methods = targetClass.getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			excludeMethods.add(method.getName());
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(excludeMethods.contains(method.getName())){
			if(logger.isInfoEnabled()){
				logger.info("ignore method | {} ...", method.toString());
			}
//			return null;
			return ReflectUtils.invokeMethod(method, this, args);
		}else if(METHOD_TO_STRING.equals(method.getName())){
			return proxyInterfaces.toString();
		}

		try {
			return this.doInvoke(proxy, method, args);
		}catch (Throwable e) {
			throw new BaseException("invoke proxy method error : " + e.getMessage(), e);
		}
		
	}

	public <T> T getProxyObject() {
		return (T)proxyObject;
	}

	abstract protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;
}
