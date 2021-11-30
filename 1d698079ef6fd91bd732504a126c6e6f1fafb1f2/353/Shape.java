package com.hlj.moudle.design.D03_生成实例.D06_Prototype模式;

import lombok.Data;
import lombok.ToString;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Shape
 * @date 2019/8/6  11:31.
 * @Description 模型类，实现了标识接口 Cloneable
 */
@Data
@ToString
public abstract class Shape implements Cloneable {

    public  String id;
    public  String type;

    abstract void draw();

    @Override
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }
}
