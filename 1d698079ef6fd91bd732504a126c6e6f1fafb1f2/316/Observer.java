package com.hlj.moudle.design.D08管理状态.D17Observer观察者模式;

/**
 * @author HealerJean
 * @ClassName Observer
 * @date 2019/8/19  20:30.
 * @Description
 */
public class Observer extends AbstractObserver {

    public void register(User user){
        this.user = user;
        //重点
        user.setObserver(this);
    }

    @Override
    public void listent( ) {
        System.out.println( "监听到的数据是:"+user.toString() );
    }
}
