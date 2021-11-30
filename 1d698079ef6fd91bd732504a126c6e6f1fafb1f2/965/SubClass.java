package com.hlj.moudle.staticT.ClassLoader.one;
/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/11  下午6:14.
 */
public class SubClass extends SuperClass{
    static{
        System.out.println("SubClass init!");
    }


    public static int s = 2; //对于静态字段，只有指定定义这个字段的类才会初始化
}
