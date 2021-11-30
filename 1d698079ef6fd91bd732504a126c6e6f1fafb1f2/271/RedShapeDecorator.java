package com.hlj.moudle.design.D05_一致性.D12Decorator装饰器模式;

/**
 * @author HealerJean
 * @ClassName RedShapeDecorator
 * @date 2019/8/14  18:32.
 * 创建扩展了 ShapeDecorator 类的实体装饰类。
 */
public class RedShapeDecorator extends AbstractShapeDecorator {

    public RedShapeDecorator(ShapeInter shapeInter) {
        super(shapeInter);
    }

    @Override
    public void draw() {
        shapeInter.draw();
        ok();
    }

    private void ok(){
        System.out.println("Border Color: Red");
    }
}
