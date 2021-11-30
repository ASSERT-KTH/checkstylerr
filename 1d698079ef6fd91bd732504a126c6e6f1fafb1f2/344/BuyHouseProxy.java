package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.D01静态代理;

import com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式.BuyHouse;

/**
 * @author HealerJean
 * @ClassName BuyHouseProxy
 * @date 2019/8/21  14:19.
 * @Description
 */
public class BuyHouseProxy implements BuyHouse {

    private BuyHouse buyHouse;

    public BuyHouseProxy(BuyHouse buyHouse) {
        this.buyHouse = buyHouse;
    }

    @Override
    public void buyHosue() {
        buyHouse.buyHosue();
    }

    @Override
    public int countAdd(int i) {
        return i++;
    }
}
