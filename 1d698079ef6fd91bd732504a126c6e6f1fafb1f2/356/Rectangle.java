package com.hlj.moudle.design.D03_生成实例.D06_Prototype模式;

import lombok.Data;
import lombok.ToString;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Rectangle
 * @date 2019/8/6  11:33.
 * @Description
 */
@Data
@ToString
public class Rectangle extends Shape {

    private String pectangleName ;

    public Rectangle(){
        type = "Rectangle";
    }

    @Override
    public void draw() {
        System.out.println(id+":"+type+":"+pectangleName);
    }
}
