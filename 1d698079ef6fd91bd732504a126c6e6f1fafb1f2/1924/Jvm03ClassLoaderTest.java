package com.hlj.moudle.Jvm03类加载器;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description
 * @Author HealerJean
 * @Date 2019/2/8  下午5:16.
 */
public class Jvm03ClassLoaderTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        //创建自定义classloader对象。
        Jvm03DiskClassLoader diskLoader = new Jvm03DiskClassLoader("/Users/healerjean/Desktop");
        try {
            //加载class文件
            Class c = diskLoader.loadClass("com.hlj.moudle.Jvm03类加载器.Jvm02Test");

            if (c != null) {
                try {
                    Object obj = c.newInstance();
                    Method method = c.getDeclaredMethod("say", null);
                    //通过反射调用Test类的say方法
                    method.invoke(obj, null);
                } catch (InstantiationException | IllegalAccessException
                        | NoSuchMethodException
                        | SecurityException |
                        IllegalArgumentException |
                        InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
