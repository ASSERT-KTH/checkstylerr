package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D02Jdk动态代理;

import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.BuyHouse;
import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.BuyHouseImpl;

import java.lang.reflect.Proxy;

/**
 * @author HealerJean
 * @ClassName DynamicProxyTest
 * @date 2019/8/21  14:22.
 * @Description
 */
public class DynamicProxyTest {

    public static void main(String[] args) {
        BuyHouse buyHouse = new BuyHouseImpl();
        System.out.println("buyHouse " + buyHouse.getClass().getName());

        DynamicProxyHandler handler = new DynamicProxyHandler(buyHouse);
        // 1、第一个参数 指定当前目标对象使用的类加载器
        // 2、第二个参数 指定目标对象实现的接口的类型 接口列表
        // 3、第三个参数 指定动态处理器，
        BuyHouse proxyBuyHouse = (BuyHouse) Proxy.newProxyInstance(BuyHouse.class.getClassLoader(), buyHouse.getClass().getInterfaces(), handler);
        System.out.println("proxyBuyHouse :" + proxyBuyHouse.getClass().getName());
        proxyBuyHouse.buyHosue();

    }

}
