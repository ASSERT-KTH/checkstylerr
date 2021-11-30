package com.hlj.moudle.design.D07简单化.D15Facade外观模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/19  18:10.
 * @Description
 */
public class Main {

    public static void main(String[] args) {
        ShapeMaker shapeMaker = new ShapeMaker();

        shapeMaker.drawCircle();
        shapeMaker.drawRectangle();
        shapeMaker.drawSquare();
    }
}

