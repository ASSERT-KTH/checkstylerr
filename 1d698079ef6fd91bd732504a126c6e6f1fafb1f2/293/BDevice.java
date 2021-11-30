package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName BDevice
 * @date 2019/8/19  18:28.
 * @Description
 */
public class BDevice extends Device {



    /**
     * 操作该设备
     */
    @Override
    public void openDevice(String instruction, AbstractMediator abstractMediator) {
        abstractMediator.openB(instruction);
    }

    @Override
    public void closeDevice(String instruction) {
        System.out.println("B设备关闭");
    }


}
