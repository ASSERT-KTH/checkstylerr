package com.hlj.moudle.design.D03_生成实例.D06_Prototype模式;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Main
 * @date 2019/8/6  11:36.
 * @Description
 */
public class Main {

        public static void main(String[] args) {

            Rectangle rectangle = new Rectangle();
            rectangle.setType("rectangle");
            rectangle.setId("1");
            rectangle.setPectangleName("rectangleName");
            ShapeCache.create(rectangle);
            Square square = new Square();
            square.setType("square");
            square.setId("2");
            square.setSquareName("squareName");
            ShapeCache.create(square);

            rectangle = (Rectangle)ShapeCache.getByType("rectangle") ;
            rectangle.draw();
            square =    (Square)ShapeCache.getByType("square") ;
            square.draw();

            // 1:rectangle:rectangleName
            // 2:square:squareName



        }
}
