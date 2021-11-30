package com.hlj.moudle.design.D06访问数据结构.D13Visitor访问模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/14  20:29.
 * @Description 使用场景，我们提前知道最后要调用什么接口实现类，但是实际处理中不知道调用哪个接口，可以预先传入接口
 */
public class Main {

    public static void main(String[] args) {
        //创建一个cpu
        BaseComputerPart cpu = new Cpu();
        //cpu连接接口
        cpu.link(new UsbImpl());
    }
}
