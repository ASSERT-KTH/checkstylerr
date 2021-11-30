package com.hlj.moudle.Jvm02垃圾收集器与内存分配策略.Jvm02引用;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * @Description 软引用
 * @Author HealerJean
 * @Date 2019/2/7  下午7:36.

   软引用需引入java.lang.ref.SoftReference类，
  所引用的对象为 仍有用但非必须的对象。被软引用关联的对象，将在抛出oom异常之前回收。
  可以应用为某些缓存内容，加快程序速度，同时又不影响内存使用。使用形式如下

 */
public class Jvm01SoftReference {


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws InterruptedException {

        //创建软引用
        ReferenceQueue<SoftReference<G>> rq = new ReferenceQueue<SoftReference<G>>();
        SoftReference[] srArr = new SoftReference[1000];

        //在 这个for循环中 new new G() 的时候
        //当执行到一定次数的是，会造成垃圾收集器回收内存(因为内存不够用了）
        for(int i = 0; i < srArr.length; i++){
            srArr[i] = new SoftReference(new G(), rq);
        }
        //获取被清除部分
        int n=0;
        for(int i = 0; i < srArr.length; i++){
            if(srArr[i].isEnqueued()){ //方法返回对象是否被垃圾回收器标记
                srArr[i]=null;
                n++;
            }
        }
        System.out.println("第一次GC,清除了"+n+"个");


        //下面的方法，会强制再执行一遍垃圾收集器，（用来测试软引用是否被回收）
        for(int i=0;i<10000;i++){
            G g=new G();
        }

        int m=0;
        for(int i = 0; i < srArr.length; i++){
            if(srArr[i]!=null&&srArr[i].isEnqueued()){
                srArr[i]=null;
                m++;
            }
        }
        System.out.println("第二次GC,清除了"+m+"个");
    }
}

//为了占据内存
class G{
    private  int [] big=new int[1000000];
}

/*
output：
第一次GC,清除了971个
第一次GC,清除了0个
*/
