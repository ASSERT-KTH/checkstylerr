package com.hlj.moudle.design.D07简单化.D15Facade外观模式;

/**
 * @author HealerJean
 * @ClassName Rectangle
 * @date 2019/8/19  18:09.
 * @Description
 */
public class Rectangle implements Shape {

    @Override
    public void draw() {
        System.out.println("Rectangle::draw()");
    }

}
