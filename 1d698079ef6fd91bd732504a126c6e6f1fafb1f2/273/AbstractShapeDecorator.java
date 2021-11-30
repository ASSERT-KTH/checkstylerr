package com.hlj.moudle.design.D05_一致性.D12Decorator装饰器模式;

/**
 * @author HealerJean
 * @ClassName AbstractShapeDecorator
 * @date 2019/8/14  18:31.
 * @ 创建实现了 Shape 接口的抽象装饰类。
 */
public abstract class AbstractShapeDecorator implements ShapeInter {

    protected ShapeInter shapeInter;

    public AbstractShapeDecorator(ShapeInter shapeInter) {
        this.shapeInter = shapeInter;
    }

    @Override
    public void draw() {
        shapeInter.draw();
    }
}
