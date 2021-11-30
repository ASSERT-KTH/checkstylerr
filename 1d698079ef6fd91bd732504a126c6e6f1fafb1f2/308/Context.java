package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName Context
 * @date 2019-08-20  21:27.
 * @Description
 */
public class Context {

    /**
     * 定义出所有的电梯状态
     */
    public final static OpenningState openningState = new OpenningState();
    public final static ClosingState closeingState = new ClosingState();
    public final static RunningState runningState = new RunningState();
    public final static StoppingState stoppingState = new StoppingState();


    /**
     * 定一个当前电梯状态
     */
    private LiftState liftState;

    public LiftState getLiftState() {
        return liftState;
    }


    /**
     * 将状态和环境绑定
     */
    public void setLiftState(LiftState liftState) {
        this.liftState = liftState;
        this.liftState.setContext(this);
    }



    public void open() {
        this.liftState.open();
    }

    public void close() {
        this.liftState.close();
    }

    public void run() {
        this.liftState.run();
    }

    public void stop() {
        this.liftState.stop();
    }
}
