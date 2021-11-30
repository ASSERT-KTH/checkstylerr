package com.hlj.moudle.design.D06访问数据结构.D13Visitor访问模式;


/**
 * 电脑的零配件的父抽象类
 */
public abstract class BaseComputerPart {
    /**
     * 所有的 零配件，都必须通过一个硬件接口进行连接
     */
    protected abstract void link(HardwareInter hardwareInter);

}
