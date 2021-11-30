package com.hlj.moudle.design.D08管理状态.D17Observer观察者模式;


/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/19  20:35.
 * @Description
 */
public class Main {

    public static void main(String[] args) {
        User user = new User();
        new Observer().register(user);
        user.login("healerjean","password");
    }
}
