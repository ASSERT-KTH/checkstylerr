package com.hlj.moudle.design.D05_一致性.D12Decorator装饰器模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/14  18:50.
 * @Description
 */
public class Main {

    public static void main(String[] args) {

        ShapeInter circle = new CircleImpl();
        System.out.println("Circle with normal border");
        circle.draw();


        AbstractShapeDecorator redCircle = new RedShapeDecorator(new CircleImpl());
        System.out.println("\nCircle of red border");
        redCircle.draw();


        AbstractShapeDecorator redRectangle = new RedShapeDecorator(new RectangleImpl());
        System.out.println("\nRectangle of red border");
        redRectangle.draw();
    }
}
