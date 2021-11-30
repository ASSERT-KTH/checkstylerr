package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式;

/**
 * @author HealerJean
 * @ClassName RefinedAbstraction
 * @date 2019/8/14  14:07.
 * @Description 扩充抽象类代码
 */
public class RefinedAbstraction extends Abstraction {

    @Override
    public void operation() {
        //代码
        impl.operation();
        //代码
    }
}
