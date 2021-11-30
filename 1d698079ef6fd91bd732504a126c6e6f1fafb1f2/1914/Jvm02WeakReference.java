package com.hlj.moudle.Jvm02垃圾收集器与内存分配策略.Jvm02引用;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * @Description
 * @Author HealerJean
 * @Date 2019/2/7  下午8:25.
 */
public class Jvm02WeakReference {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws InterruptedException {
        //创建弱引用
        ReferenceQueue<WeakReference<G>> rq = new ReferenceQueue<WeakReference<G>>();
        WeakReference[] srArr = new WeakReference[1000];

        for(int i = 0; i < srArr.length; i++){
            srArr[i] = new WeakReference(new G(), rq);
        }
        //获取被清除部分
        int n=0;
        for(int i = 0; i < srArr.length; i++){
            if(srArr[i].isEnqueued()){
                srArr[i]=null;
                n++;
            }
        }
        System.out.println("第一次GC,清除了"+n+"个");

        //尝试请求一次GC,防止上面不执行垃圾收集器
        System.gc();

        //获取第二次被清除部分
        int m=0;
        for(int i = 0; i < srArr.length; i++){
            if(srArr[i]!=null&&srArr[i].isEnqueued()){
                srArr[i]=null;
                m++;
            }
        }
        System.out.println("第一次GC,清除了"+m+"个");
    }
}
/*
output （第二次清除个数有明显变动）
第一次GC,清除了965个
第一次GC,清除了16个
*/
