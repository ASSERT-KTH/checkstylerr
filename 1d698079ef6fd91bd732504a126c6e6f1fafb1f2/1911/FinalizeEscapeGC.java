package com.hlj.moudle.Jvm02垃圾收集器与内存分配策略.Jvm03Finalize;


/*
 * @Description 垃圾收集器 是对象逃脱
 * @Author HealerJean
 * @Date 2018/4/9  下午3:30.
 *
 *此代码演示了两点
 * 对象可以在GC时自我拯救
 * 这种自救只会有一次，因为一个对象的finalize方法只会被自动调用一次
 * */
public class FinalizeEscapeGC {
    public static FinalizeEscapeGC SAVE_HOOK=null;
    public void isAlive(){
        System.out.println("yes我还活着");
    }

    public void finalize() throws Throwable{
        super.finalize();
        System.out.println("执行finalize方法");
        FinalizeEscapeGC.SAVE_HOOK=this;//自救
    }

    public static void main(String[] args) throws InterruptedException{
        SAVE_HOOK=new FinalizeEscapeGC();


        //对象的第一次回收
        SAVE_HOOK=null;
        System.gc();
        //因为finalize方法的优先级很低所以暂停0.5秒等它
        Thread.sleep(500);
        if(SAVE_HOOK!=null){
            SAVE_HOOK.isAlive();
        }else{
            System.out.println("no我死了");
        }


        //下面的代码和上面的一样，但是这次自救却失败了，因为finalize方法已经调用过一次，而且它只能执行一次
        //对象的第二次回收
        SAVE_HOOK=null;
        System.gc();
        Thread.sleep(500);
        if(SAVE_HOOK!=null){
            SAVE_HOOK.isAlive();
        }else{
            System.out.println("no我死了");
        }
    }
}