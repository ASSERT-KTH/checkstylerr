package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName ADevice
 * @date 2019/8/19  18:26.
 * @Description 窗帘
 */
public class ADevice extends Device{


    @Override
    public void openDevice(String instruction, AbstractMediator mediator) {
        System.out.println("A设备正在"+instruction);
        mediator.openA(instruction);
    }

    @Override
    public void closeDevice(String instruction) {
        System.out.println("A设备关闭");
    }

}
