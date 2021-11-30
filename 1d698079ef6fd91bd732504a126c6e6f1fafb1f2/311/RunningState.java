package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName RunningState
 * @date 2019-08-20  21:30.
 * @Description电梯在运行状态下能做哪些动作
 */

public class RunningState extends LiftState {

    @Override
    public void open() {
        throw new RuntimeException("电梯在运行状态下门不可以打开");
    }


    @Override
    public void close() {
        throw new RuntimeException("电梯在运行状态下门肯定是关闭的");
    }

    @Override
    public void run() {
        System.out.println("电梯正在开始跑…………………………");
    }

    @Override
    public void stop() {
        System.out.println("电梯准备从Running状态到Stoping状态");
        super.context.setLiftState(Context.stoppingState);
        super.context.getLiftState().stop();
    }
}
