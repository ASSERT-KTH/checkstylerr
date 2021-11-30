package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName AbstractMediator
 * @date 2019/8/19  18:15.
 * @Description 仲裁者
 */
public abstract class AbstractMediator {

    /**
     *保留所有设备的引用是为了当接收指令时可以唤醒其他设备的操作
     */
    Device a , b , c;

    public AbstractMediator(Device a, Device b, Device c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * 定义操作属性
     */
    public abstract void openA(String instruction);
    public abstract void openB(String instruction);
    public abstract void openC(String instruction);

}
