package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ColdDrinkPepsi
 * @date 2019/8/6  12:58.
 * @Description
 */
public class ColdDrinkPepsi extends ColdDrink {

    @Override
    public float price() {
        return 35.0f;
    }

    @Override
    public String name() {
        return "百事可乐";
    }
}
