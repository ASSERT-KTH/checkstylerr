package com.hlj.moudle.design.D03_生成实例.D06_Prototype模式;

import lombok.Data;
import lombok.ToString;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Square
 * @date 2019/8/6  11:33.
 * @Description
 */
@Data
@ToString
public class Square extends Shape {

    private String squareName ;

    public Square(){
        type = "Square";
    }

    @Override
    public void draw() {
        System.out.println(id+":"+type+":"+squareName);
    }
}
