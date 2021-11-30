package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D03CGLIB代理;

import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.BuyHouseImpl;

/**
 * @author HealerJean
 * @ClassName CglibProxyTest
 * @date 2019/8/21  14:29.
 * @Description
 */
public class CglibProxyTest {

    public static void main(String[] args){
        BuyHouseImpl buyHouse = new BuyHouseImpl();
        CglibProxy cglibProxy = new CglibProxy();
        BuyHouseImpl buyHouseCglibProxy = cglibProxy.getInstance(buyHouse,BuyHouseImpl.class);
        buyHouseCglibProxy.buyHosue();
    }
}
