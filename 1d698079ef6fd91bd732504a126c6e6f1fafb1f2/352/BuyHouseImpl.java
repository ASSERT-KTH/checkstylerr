package com.hlj.moudle.design.D09避免浪费.D21Proxy代理模式;

/**
 * @author HealerJean
 * @ClassName BuyHouseImpl
 * @date 2019/8/21  14:19.
 * @Description
 */
public class BuyHouseImpl implements BuyHouse {

    @Override
    public void buyHosue() {
        System.out.println("我要买房");
    }

    @Override
    public int countAdd(int i) {
        return i++;
    }

}
