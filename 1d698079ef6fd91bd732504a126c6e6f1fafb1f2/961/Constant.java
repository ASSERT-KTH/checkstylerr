package com.hlj.moudle.staticT.ClassLoader.two;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/11  下午6:24.
 */
public class Constant {
    static{
        System.out.println("constant class init!");
    }
    public final static int a = 1; ;// 非final的时候，可以触发上面的
}