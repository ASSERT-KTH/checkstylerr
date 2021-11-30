package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D02Jdk动态代理;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author HealerJean
 * @ClassName DynamicProxyHandler
 * @date 2019/8/21  14:22.
 * @Description
 */
public class DynamicProxyHandler implements InvocationHandler {

    private Object object;

    public DynamicProxyHandler(final Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(object, args);
        return result;
    }
}
