package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName CDevice
 * @date 2019/8/19  18:16.
 * @Description
 */
public class CDevice extends Device {


    @Override
    public void openDevice(String instruction, AbstractMediator abstractMediator) {
        abstractMediator.openC(instruction);
    }

    @Override
    public void closeDevice(String instruction) {
        System.out.println("C设备关闭");
    }



}
