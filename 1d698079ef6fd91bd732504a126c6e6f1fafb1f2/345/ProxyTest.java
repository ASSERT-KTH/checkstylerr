package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D01静态代理;

import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.BuyHouse;
import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.BuyHouseImpl;

/**
 * @author HealerJean
 * @ClassName ProxyTest
 * @date 2019/8/21  14:19.
 * @Description
 */
public class ProxyTest {
    public static void main(String[] args) {
        BuyHouse buyHouse = new BuyHouseImpl();
        buyHouse.buyHosue();
        BuyHouseProxy buyHouseProxy = new BuyHouseProxy(buyHouse);
        buyHouseProxy.buyHosue();
    }
}
