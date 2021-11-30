package com.hlj.moudle.design.D06访问数据结构.D13Visitor访问模式;

/**
 * @author HealerJean
 * @ClassName UsbImpl
 * @date 2019/8/14  20:28.
 * @Description
 */
public class UsbImpl implements  HardwareInter{

    @Override
    public void visitor(Cpu cpu) {
        System.out.println("usb连接cpu");
    }
}
