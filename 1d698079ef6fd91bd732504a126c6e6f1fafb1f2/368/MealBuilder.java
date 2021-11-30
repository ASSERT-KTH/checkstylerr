package com.hlj.moudle.design.D03_生成实例.D07_Builer建造者模式_组装复杂的实例;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName MealBuilder
 * @date 2019/8/6  13:01.
 * @Description
 */
public class MealBuilder {

    public Meal prepareVegMeal (){
        Meal meal = new Meal();
        meal.addItem(new BurgerVeg());
        meal.addItem(new ColdDrinkCoke());
        return meal;
    }

    public Meal prepareNonVegMeal (){
        Meal meal = new Meal();
        meal.addItem(new BurgerChicken());
        meal.addItem(new ColdDrinkPepsi());
        return meal;
    }
}
