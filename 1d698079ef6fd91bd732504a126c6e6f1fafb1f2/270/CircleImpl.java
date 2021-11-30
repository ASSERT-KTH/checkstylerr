package com.hlj.moudle.design.D05_一致性.D12Decorator装饰器模式;

/**
 * @author HealerJean
 * @ClassName CircleImpl
 * @date 2019/8/14  18:27.
 * @Description
 */
public class CircleImpl implements ShapeInter {

    @Override
    public void draw() {
        System.out.println("Shape: Circle");
    }
}
