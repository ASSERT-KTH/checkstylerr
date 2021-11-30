package com.hlj.moudle.staticT.ClassLoader.one;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/11  下午6:13.
 */

public class SuperClass {
    static{
        System.out.println("super class init!");
    }
    public static int a = 1;
    public final static int b = 1;
}