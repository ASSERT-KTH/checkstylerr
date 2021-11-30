package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName Device
 * @date 2019/8/19  18:14.
 * @Description
 */
public abstract class Device {


    public abstract void openDevice(String instruction, AbstractMediator abstractMediator);


    public abstract void closeDevice(String instruction);

}
