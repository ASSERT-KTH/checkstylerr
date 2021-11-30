package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName ClosingState
 * @date 2019-08-20  21:29.
 * @Description 电梯门关闭以后，电梯可以做哪些事情
 */
public class ClosingState extends LiftState {



    @Override
    public void open() {
        System.out.println("电梯准备从Closing状态到Opening状态");
        super.context.setLiftState(Context.openningState);
        super.context.getLiftState().open();
    }

    /**
     *  电梯门关闭，这是关闭状态要实现的动作
     */
    @Override
    public void close() {
        System.out.println("电梯门关闭...");
    }


    @Override
    public void run() {
        System.out.println("电梯准备从Closing状态到Runing状态");
        super.context.setLiftState(Context.runningState);
        super.context.getLiftState().run();
    }

    @Override
    public void stop() {
        System.out.println("电梯准备从Closing状态到Stoping状态");
        super.context.setLiftState(Context.stoppingState);
        super.context.getLiftState().stop();
    }
}
