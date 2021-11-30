package com.hlj.moudle.design.D07简单化.D15Facade外观模式;

/**
 * @author HealerJean
 * @ClassName ShapeMaker
 * @date 2019/8/19  18:09.
 * @Description
 */
public class ShapeMaker {
    private Shape circle;
    private Shape rectangle;
    private Shape square;

    public ShapeMaker() {
        circle = new Circle();
        rectangle = new Rectangle();
        square = new Square();
    }

    public void drawCircle(){
        circle.draw();
    }
    public void drawRectangle(){
        rectangle.draw();
    }
    public void drawSquare(){
        square.draw();
    }
}
