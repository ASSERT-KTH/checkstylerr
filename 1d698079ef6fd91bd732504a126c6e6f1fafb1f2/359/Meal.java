package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

import com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例.inter.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Meal
 * @date 2019/8/6  12:59.
 * @Description 定义商品集合 套餐
 */

public class Meal {

    /** 商品集合 */
    private List<Item> items = new ArrayList<Item>();

    public void addItem(Item item){
        items.add(item);
    }

    /** 商品总价格 */
    public float getCost(){
        float cost = 0.0f;
        for (Item item : items) {
            cost += item.price();
        }
        return cost;
    }

    /** 显示所有商品 */
    public void showItems(){
        for (Item item : items) {
            System.out.print("商品名称： : "+item.name());
            System.out.print(", 打包方式 : "+item.packing().pack());
            System.out.println(", 价格 : "+item.price());
        }
    }
}
