package com.hlj.moudle.design.D07简单化.D16Mediator中介者模式;

/**
 * @author HealerJean
 * @ClassName ConcreteAbstractMediator
 * @date 2019/8/19  18:22.
 * @Description
 */
public class ConcreteAbstractMediator extends AbstractMediator {

    public ConcreteAbstractMediator(Device a, Device b, Device c) {
        super(a, b, c);
    }

    /**
     * 开启A要关闭B和C
     * @param instruction
     */
    @Override
    public void openA(String instruction) {
         b.closeDevice(instruction);
         c.closeDevice(instruction);
    }


    @Override
    public void openB(String instruction) {

    }

    @Override
    public void openC(String instruction) {

    }
}
