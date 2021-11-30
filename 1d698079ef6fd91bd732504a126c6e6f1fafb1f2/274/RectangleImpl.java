package com.hlj.moudle.design.D05_一致性.D12Decorator装饰器模式;

/**
 * @author HealerJean
 * @ClassName RectangleImpl
 * @date 2019/8/14  18:29.
 * @Description
 */
public class RectangleImpl implements ShapeInter {

    @Override
    public void draw() {
        System.out.println("Shape: Rectangle");
    }
}
