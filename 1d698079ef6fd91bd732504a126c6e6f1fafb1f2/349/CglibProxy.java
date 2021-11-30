package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D03CGLIB代理;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author HealerJean
 * @ClassName CglibProxy
 * @date 2019/8/21  14:28.
 * @Description
 */
public class CglibProxy implements MethodInterceptor {

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Object target, Class<T> clazz) {
        //字节码加强器：用来创建动态代理类
        Enhancer enhancer = new Enhancer();
        //代理的目标对象
        enhancer.setSuperclass(target.getClass());
        //回调类，在代理类方法调用时会回调Callback类的intercept方法
        enhancer.setCallback(this);

        //创建代理类
        Object result = enhancer.create();
        return (T)result;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        //调用目标类（父类）的方法
        Object result = proxy.invokeSuper(obj, args);
        return result;
    }

}
