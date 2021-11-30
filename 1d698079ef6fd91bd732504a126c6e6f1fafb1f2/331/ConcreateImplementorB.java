package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式;

/**
 * @author HealerJean
 * @ClassName ConcreateImplementorB
 * @date 2019/8/14  14:04.
 * @Description
 */
public class ConcreateImplementorB implements Implementor {

    @Override
    public void operation() {
        System.out.println("this is concreteImplementorB's operation...");
    }
}
