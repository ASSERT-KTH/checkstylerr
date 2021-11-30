package com.hlj.moudle.design.D08管理状态.D17Observer观察者模式;


/**
 * @author HealerJean
 * @ClassName AbstractObserver
 * @date 2019/8/19  20:30.
 * @Description
 */
public abstract  class AbstractObserver {

    /**
     * 被观察的对象
     */
    public User user ;

    public abstract void listent() ;

}
