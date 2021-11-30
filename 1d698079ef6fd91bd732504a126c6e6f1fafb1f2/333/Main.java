package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/14  14:07.
 */
public class Main {

    public static void main(String[] args) {
        Abstraction abstraction = new RefinedAbstraction();

        //调用第一个实现类
        abstraction.setImpl(new ConcreateImplementorA());
        abstraction.operation();

        //调用第二个实现类
        abstraction.setImpl(new ConcreateImplementorB());
        abstraction.operation();

    }
}
