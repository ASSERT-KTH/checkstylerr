package com.hlj.moudle.staticT.ClassLoader.one;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/11  下午6:14.
 */
public class Test {

    static{
        System.out.println("test class init!");
    }

    public static void main(String[] args){
        System.out.println(SubClass.s);
    }
}