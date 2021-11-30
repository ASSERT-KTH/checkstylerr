package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式;

import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D01静态代理.BuyHouseProxy;
import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D02Jdk动态代理.DynamicProxyHandler;
import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D03CGLIB代理.CglibProxy;

import java.lang.reflect.Proxy;
import java.time.LocalDate;

/**
 * @author HealerJean
 * @ClassName FastMain
 * @date 2019/8/21  16:54.
 * @Description 看看那个比较快
 */
public class FastMain {

    public static void main(String[] args) {

        BuyHouse buyHouse = new BuyHouseImpl();

        Long norStart = System.currentTimeMillis() ;
        for(int i = 1 ; i < 10000000 ; i++ ){
            buyHouse.countAdd(i);
        }
        System.out.println("普通："+(System.currentTimeMillis()-norStart));


        norStart = System.currentTimeMillis() ;
        DynamicProxyHandler handler = new DynamicProxyHandler(buyHouse);
        BuyHouse jdkBuyHouse = (BuyHouse) Proxy.newProxyInstance(BuyHouse.class.getClassLoader(), buyHouse.getClass().getInterfaces(), handler);
        for(int i = 1 ; i < 10000000 ; i++ ){
            jdkBuyHouse.countAdd(i);
        }
        System.out.println("JDK："+(System.currentTimeMillis()-norStart));


        norStart = System.currentTimeMillis() ;
        CglibProxy cglibProxy = new CglibProxy();
        BuyHouseImpl buyHouseCglibProxy = cglibProxy.getInstance(buyHouse,BuyHouseImpl.class);
        for(int i = 1 ; i < 10000000 ; i++ ){
            buyHouseCglibProxy.countAdd(i);
        }
        System.out.println("CGLIB："+(System.currentTimeMillis()-norStart));


    }



}
