package com.hlj.moudle.design.D06访问数据结构.D13Visitor访问模式;

/**
 * 电脑的硬件CPU，用于数据的运算
 */
public class Cpu extends BaseComputerPart {

    @Override
    protected void link(HardwareInter hardwareInterface) {

        // 先得通过接口连接数据
        hardwareInterface.visitor(this);
        // 连接完了之后，就开始使用cpu
        System.out.println("连接上了之后，利用cpu进行计算数据");
    }

}
