package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName LiftState
 * @date 2019-08-20  21:21.
 * @Description 定义一个电梯的接口
 */
public abstract class LiftState {

    /**
     * 定义一个环境角色，也就是封装状态的变换引起的功能变化
     */
    protected Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 电梯门开启
     */
    public abstract void open();

    /**
     * 电梯门关闭
     */
    public abstract void close();

    /**
     * 电梯跑起来
     */
    public abstract void run();

    /**
     * 电梯还要能停下来
     */
    public abstract void stop();


}
