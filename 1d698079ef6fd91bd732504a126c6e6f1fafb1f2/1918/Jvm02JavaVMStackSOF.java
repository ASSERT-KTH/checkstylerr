package com.hlj.moudle.Jvm01内存分配;



/*

 * @Description 虚拟机栈和本地方法栈溢出
 * @Author HealerJean
 * @Date 2019/2/7  下午6:24.

  JvmAgs : -Xss128k

 */
public class Jvm02JavaVMStackSOF {
    private int stackLength = 1;//栈内存

    public void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public static void main(String[] args) throws Throwable {
        Jvm02JavaVMStackSOF oom = new Jvm02JavaVMStackSOF();
        try {
            oom.stackLeak();
        } catch (Throwable e) {
            System.out.println("stack length：" + oom.stackLength);
            throw e;
        }
    }
}