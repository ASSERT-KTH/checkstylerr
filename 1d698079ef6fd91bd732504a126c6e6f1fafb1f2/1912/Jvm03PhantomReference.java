package com.hlj.moudle.Jvm02垃圾收集器与内存分配策略.Jvm02引用;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * @Description
 * @Author HealerJean
 * @Date 2019/2/7  下午8:29.
 */
public class Jvm03PhantomReference {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public static void main(String[] args) throws InterruptedException {
            //创建弱引用
            ReferenceQueue<PhantomReference<G3>> rq = new ReferenceQueue<PhantomReference<G3>>();
            PhantomReference[] srArr = new PhantomReference[1000];

            for(int i = 0; i < srArr.length; i++){
                G3 g=new G3();
                srArr[i] = new PhantomReference(g, rq);
                //g = null;

            }
            //获取被清除部分
            int n = 0;
            for(int i = 0; i < srArr.length; i++){
                if(srArr[i].isEnqueued()){
                    srArr[i] = null;
                    n++;
                }
            }
            System.out.println("清除了"+n+"个");
        }
    }
    //为了占据内存
    class G3{
        private  int [] big=new int[1000000];
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            big=null;
        }
    }
/*
output
清除了826个
*/
