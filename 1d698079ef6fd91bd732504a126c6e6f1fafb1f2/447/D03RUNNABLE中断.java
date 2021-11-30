package com.hlj.moudle.thread.d06Interupt线程中断;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午3:53.
 * 类描述：
 */
public class D03RUNNABLE中断 {

    @Test
    public void testRUNNABLE中断测试(){

        Thread thread = new Thread(()->{
            for (int i = 0; i < 100; i++) {
                if(Thread.currentThread().isInterrupted()){
                    System.out.println("\n程序已经被中断了，结束程序运行");
                    return;
                }
                System.out.print(i+""+Thread.currentThread().isInterrupted()+"___");
            }
        });

        thread.start();
        try {
            Thread.sleep(1); //确保上面的线程在执行过程中进行中断
            thread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * 打印结果
         0false___1false___2false___3false___4false___5false___6false___7false___8false___9false___10false___11false___12false___13false___14false___15false___16false___17false___18false___19false___20false___21false___22false___23false___24false___25false___26false___27false___28false___29false___30false___31false___32false___33false___34false___35false___36false___37false___38false___39false___40false___41false___42false___43false___44false___45false___46false___47false___48false___49false___50false___51false___52false___53false___54false___55false___56false___57false___58false___59false___60false___61false___62false___63false___64false___65false___66false___67false___68false___69false___70false___71false___72false___73false___74false___75false___76true___77true___78true___79true___80true___81true___82true___83true___84true___85true___86true___87true___88true___89true___90true___91true___92true___93true___94true___95true___96true___97true___98true___99true___         */

    }

}
