package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/19  18:28.
 * @Description
 */
public class Main {

    public static void main(String[] args) {
        Device a = new ADevice();
        Device b = new BDevice();
        Device c = new CDevice();

        //把设备引用都保存在调停者中
        AbstractMediator mediator=new ConcreteAbstractMediator(a , b ,c );

        a.openDevice("开启",mediator);
    }
}
