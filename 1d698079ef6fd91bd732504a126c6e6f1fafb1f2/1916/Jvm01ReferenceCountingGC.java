package com.hlj.moudle.Jvm02垃圾收集器与内存分配策略;

/**
 * @Description 引用计数算法
 * @Author HealerJean
 * @Date 2018/4/9  下午1:38.
 * 给对象添加一个计时器，每当引用的时候加1，当引用失效时候减1，任何时候为0的对象就是不能再被使用的。（书上说，这样表达不太好）
 *
 *  -XX:+PrintGCDetails
 *
 */
public class Jvm01ReferenceCountingGC {

    public Object instance = null;

    private static  final int _1MB=1024 * 1024 ;

    /**
     占点内存，以便在日志中看清楚是否被回收
     */
    private byte[] bigSize = new byte[1 * _1MB];


    public static void main(String[] args) {
        Jvm01ReferenceCountingGC objA = new Jvm01ReferenceCountingGC();
        Jvm01ReferenceCountingGC objB = new Jvm01ReferenceCountingGC();

        objA.instance = objB;
        objB.instance = objA;

        //加入这个时候发生GC，如果是jvm采用的是引用计数法的话，objA和objB不能被回收，被回收了，说明不是采用的引用计数法
        // 因为他们互相引用这对方，导致他们的引用计数都不为0，
        // 于是引用计数算法，不能通知GC收集器回收他们
        System.gc(); //垃圾收集器回收内存
        }

}
