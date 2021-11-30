package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName OpenningState
 * @date 2019-08-20  21:23.
 * @Description 在电梯门开启的状态下能做什么事情
 */

public class OpenningState extends LiftState {


    @Override
    public void close() {
        System.out.println("电梯准备从Openning状态到Closeing状态");
        super.context.setLiftState(Context.closeingState);
        super.context.getLiftState().close();
    }


    @Override
    public void open() {
        System.out.println("电梯门开启...");
    }


    @Override
    public void run() {
        throw new RuntimeException("电梯在门开启下就是不可以运行，容易发生危险");
    }


    @Override
    public void stop() {
        throw new RuntimeException("电梯在门开启下就是停止状态，无须操作");

    }



}
