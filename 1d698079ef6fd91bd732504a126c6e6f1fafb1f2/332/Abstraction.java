package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式;

/**
 * @author HealerJean
 * @ClassName Abstraction
 * @date 2019/8/14  14:05.
 * 桥接类Abstraction，其中有对Implementor接口的引用：
 */
public abstract class Abstraction
{
    public Implementor impl;

    public void setImpl(Implementor impl) {
        this.impl=impl;
    }

    public abstract void operation();
}
