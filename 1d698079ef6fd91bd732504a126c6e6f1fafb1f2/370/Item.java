package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例.inter;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Item
 * @date 2019/8/6  12:54.
 * @Description 商品属性
 */
public interface Item {

    /**  商品名称 */
     String name();

    /**  商品价格 */
    float price();

    /**  打包方式 */
    Pack packing();

}
