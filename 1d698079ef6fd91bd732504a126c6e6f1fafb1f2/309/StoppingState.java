package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName StoppingState
 * @date 2019-08-20  21:31.
 * @Description 在停止状态下能做什么事情
 */
public class StoppingState extends LiftState {

    @Override
    public void open() {
        System.out.println("电梯准备从Stopping状态到Opening状态");
        super.context.setLiftState(Context.openningState);
        super.context.getLiftState().open();
    }

    @Override
    public void close() {
        throw new RuntimeException("电梯停止状态下们就是关闭的");
    }


    @Override
    public void run() {
        System.out.println("电梯准备从Stopping状态到Running状态");
        super.context.setLiftState(Context.runningState);
        super.context.getLiftState().run();
    }


    @Override
    public void stop() {
        System.out.println("电梯停止了...");
    }
}
