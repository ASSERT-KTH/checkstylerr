package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式;

/**
 * @author HealerJean
 * @ClassName ConcreateImplementorA
 * @date 2019/8/14  14:03.
 * @Description 接口实现类A
 */
public class ConcreateImplementorA implements Implementor {

    @Override
    public void operation() {
        System.out.println("this is concreteImplementorA's operation...");
    }
}
