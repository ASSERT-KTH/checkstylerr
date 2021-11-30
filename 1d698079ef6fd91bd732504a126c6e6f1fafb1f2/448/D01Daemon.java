package com.hlj.moudle.thread.d08Daemon守护线程;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午5:00.
 * 类描述：

 守护线程，是在后台默默的完成一些系统的服务，比如垃圾回收线程，与之相对于的就是用户线程，
 用户线程可以认为是系统的工作线程，如果用户线程全部结束，也就意味着，这个应用程序无事可做了，
 那么守护线程要守护的对象也就不存在了，整个应用程序也就结束了，必须在start之前设置  thread.setDaemon(true);否则会跑出一个IllegalThreadStateException异常。你不能把正在运行的常规线程设置为守护线程。
 注意事项 ：

(2) 不要认为所有的应用都可以分配给Daemon来进行服务，比如读写操作或者计算逻辑。因为不可能知道在所有的User完成之前，Daemon是否已经完成了预期的服务任务。一旦User退出了，可能大量数据还没有来得及读入或写出，计算任务也可能多次运行结果不一样。这对程序是毁灭性的。造成这个结果理由已经说过了：一旦所有User Thread离开了，虚拟机也就退出运行了。


 *
 *
 */
public class D01Daemon {

        @Test
        public void testJoin(){

            Thread thread = new Thread(()->{
                int i = 1 ;
                while (true){
                    System.out.printf("_"+(i++));
                }

            });


            thread.setDaemon(true);//进程守护，主线程执行完毕，它就结束运行吧
            thread.start();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\n主程序结束运行");


            /**

             _1_2_3_4_5_6_7_8_9_10_11_12_13_14_15_16_17_18_19_20_21_22_23
             主程序结束运行
             _24_25_26_27_28_29_30_31_32_33_34_35_36_37_38_39_40_41_42_43_44_45_46_47_48_49_50_51_52_53
             Process finished with exit code 0
             _54_55_56_57_58_59_60_61_62_63_64_65_66_67_68_69_70_71_72_73_74_75_76_77_78_79_80_81_82_83_84_85

             */
        }


}
