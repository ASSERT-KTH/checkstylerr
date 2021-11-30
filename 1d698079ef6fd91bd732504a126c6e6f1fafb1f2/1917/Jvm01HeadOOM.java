package com.hlj.moudle.Jvm01内存分配;/*




/**
 * @Description Java堆溢出
 * @Author HealerJean
 * @Date 2019/2/7  下午5:12.

   JVM   -Xms20m -Xmx20m
 */

import java.util.ArrayList;
import java.util.List;

public class Jvm01HeadOOM {


    static class OOMObject{}

    public static void main(String[] args) {
        List<OOMObject> oomObjects = new ArrayList<>();

        while (true){
            oomObjects.add(new OOMObject());
        }
    }
}