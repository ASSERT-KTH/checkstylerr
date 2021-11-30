package com.hlj.moudle.design.D08管理状态.D17Observer观察者模式;

import lombok.Data;

/**
 * @author HealerJean
 * @ClassName User
 * @date 2019/8/19  20:38.
 * @Description
 */
@Data
public class User {

    private String  name ;
    private String password ;


    /**
     * 观察者
     */
    private AbstractObserver observer;


    public void login(String name, String password){
        System.out.println("正在登陆的用户为："+name+"密码为："+password);
        observer.listent();
    }

    /**
     * 重点
     * @param observer
     */
    public void setObserver(AbstractObserver observer){
        this.observer = observer;
    }

}
