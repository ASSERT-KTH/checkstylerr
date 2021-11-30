package com.hlj.moudle.design.D03_生成实例.D06_Prototype模式;

import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ShapeCache
 * @date 2019/8/6  11:34.
 * @Description
 */
public class ShapeCache {

    private static Map<String, Shape> shapeMap   = new HashMap<>();

    /**
     * 通过map和拷贝 获取全新对象
     */
    public static Shape getByType(String shapeId) {
        Shape cachedShape = shapeMap.get(shapeId);
        return (Shape) cachedShape.clone();
    }

    /**
     * 创建原型
     */
    public static void create(Shape shape) {
        shapeMap.put(shape.getType(),shape);
    }
}
