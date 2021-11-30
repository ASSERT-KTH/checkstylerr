package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ColdDrinkCoke
 * @date 2019/8/6  12:57.
 * @Description
 */
public class ColdDrinkCoke extends ColdDrink {

    @Override
    public float price() {
        return 30.0f;
    }

    @Override
    public String name() {
        return "可口可乐";
    }
}
