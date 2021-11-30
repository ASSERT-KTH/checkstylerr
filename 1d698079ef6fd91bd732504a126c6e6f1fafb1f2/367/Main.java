package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Main
 * @date 2019/8/6  13:05.
 * @Description
 */
public class Main {

    public static void main(String[] args) {
        //建造者
        MealBuilder mealBuilder = new MealBuilder();

        Meal oneMeal = mealBuilder.prepareVegMeal();
        System.out.println("套餐一");
        oneMeal.showItems();
        System.out.println("总价格: " +oneMeal.getCost());
        System.out.println();

        Meal twoMeal = mealBuilder.prepareNonVegMeal();
        System.out.println("套餐二");
        twoMeal.showItems();
        System.out.println("总价格: " +twoMeal.getCost());


        // 套餐一
        // 商品名称： : 蔬菜汉堡, 打包方式 : 塑料袋, 价格 : 25.0
        // 商品名称： : 可口可乐, 打包方式 : 瓶装, 价格 : 30.0
        // 总价格: 55.0
        //
        // 套餐二
        // 商品名称： : 鸡腿汉堡, 打包方式 : 塑料袋, 价格 : 50.5
        // 商品名称： : 百事可乐, 打包方式 : 瓶装, 价格 : 35.0
        // 总价格: 85.5
    }
}
