package com.hlj.moudle.Jvm01内存分配;


import java.util.ArrayList;
import java.util.List;

/**
 * @Description 3、 方法区和运行时常亮溢出
 * @Author HealerJean
 * @Date 2019/2/7  下午6:47.

  VM Args： -XX:PermSize=10M -XX:MaxPermSize=10M


 */
public class Jvm03RuntimeConstantPoolOOM {

    public static void main(String[] args) {
        // 使用 List 保持着常量池引用，避免 Full GC 回收常量池行为
        List<String> list = new ArrayList<String>();
        // 10MB 的 PermSize 在 integer 范围内足够产生 OOM 了
        int i = 0;
        while (true) {
            list.add(String.valueOf(i++).intern());
        }
    }

}
