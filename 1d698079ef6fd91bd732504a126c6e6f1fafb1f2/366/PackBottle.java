package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

import com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例.inter.Pack;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName PackBottle
 * @date 2019/8/6  12:55.
 * @Description
 */
public class PackBottle implements Pack {

    @Override
    public String pack() {
        return "瓶装";
    }
}
