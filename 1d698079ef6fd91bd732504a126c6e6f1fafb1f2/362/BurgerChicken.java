package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName BurgerChicken
 * @date 2019/8/6  12:56.
 * @Description
 */
public class BurgerChicken extends Burger {

    @Override
    public float price() {
        return 50.5f;
    }

    @Override
    public String name() {
        return "鸡腿汉堡";
    }
}
