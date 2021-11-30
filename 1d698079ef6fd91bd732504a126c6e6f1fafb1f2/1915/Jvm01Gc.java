package com.hlj.moudle.Jvm02垃圾收集器与内存分配策略.Jvm04回收策略;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/9  下午6:23.
 * 1KB是1024字节 ，1MB是1024KB,    1GB是1024MB
 * 1M=1024K,在电脑中1K=1KB=1024Bytes 一般卖的硬盘算法 1K=1KB=1000Bytes
 */
public class Jvm01Gc {
    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args)
    {
        testAllocation();
//        testPretenureSizeThreshold();
//        testTenuringThreshold();
    }

    /**
     * 1、对象有限在Eden分配
     * 参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
     * 输出gc日志， 堆内存初始化大小20M，堆内存最大20M，新生代大小10M，那么剩余分配给老年代就是10M， 输出GC的详细日志，
     * Eden的区域是一个survivor区域的8倍 就是说比为 8：1 也就是说新生代做多能后去到 8M
     */
    public static void testAllocation()
    {
        byte[] allocation1, allocation2, allocation3, allocation4;

        allocation1 = new byte[2 * _1MB];    //申请两兆
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        //这里我们再eden已经申请了6M的空间，
        // 而实际上新生代大小是EDEN + 一个survivor= 9M  Eden=8M survivor两块分别1M（因为复制算法的原因）
        allocation4 = new byte[4 * _1MB];
    }

    /**
        2、大对象应该直接放到老生代中
     */

    public static void testPretenureSizeThreshold(){
        byte[] allocation;
        allocation = new byte[4 * _1MB];
    }



    /**3、长期存活的对象将进入老年代
     * VM参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1
     * -XX:+PrintTenuringDistribution
     */
    @SuppressWarnings("unused")
    public static void testTenuringThreshold() {
        byte[] allocation1, allocation2, allocation3;
        allocation1 = new byte[_1MB / 4];
        // 什么时候进入老年代取决于XX:MaxTenuringThreshold设置
        allocation2 = new byte[4 * _1MB];
        allocation3 = new byte[4 * _1MB];
        allocation3 = null;
        allocation3 = new byte[4 * _1MB];
    }

    /**
     * 4、动态对象年龄判断
     * VM参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=15
     * -XX:+PrintTenuringDistribution
     */
    @SuppressWarnings("unused")
    public static void testTenuringThreshold2() {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[_1MB / 4];   // allocation1+allocation2大于survivo空间一半
        allocation2 = new byte[_1MB / 4];
        allocation3 = new byte[4 * _1MB];
        allocation4 = new byte[4 * _1MB];
        allocation4 = null;
        allocation4 = new byte[4 * _1MB];
    }



    /**
     * 5、空间分配担保
     * VM参数：-Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:-HandlePromotionFailure=false/true
     */
    @SuppressWarnings("unused")
    public static void testHandlePromotion() {
        byte[] allocation1, allocation2, allocation3, allocation4, allocation5, allocation6, allocation7;
        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        allocation1 = null;
        allocation4 = new byte[2 * _1MB];
        allocation5 = new byte[2 * _1MB];
        allocation6 = new byte[2 * _1MB];
        allocation4 = null;
        allocation5 = null;
        allocation6 = null;
        allocation7 = new byte[2 * _1MB];
    }


}
