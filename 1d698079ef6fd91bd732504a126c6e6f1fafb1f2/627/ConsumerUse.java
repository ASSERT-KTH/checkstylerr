package com.hlj.java9.Consumer;

import com.hlj.java9.api.MyServiceInter;
//import com.hlj.java9.api.impl.MyServiceInterImpl;

import java.util.ServiceLoader;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/10/10  上午10:23.
 */
public class ConsumerUse {
    public static void main(String[] args) {

        //专门用来提供服务的类
        ServiceLoader<MyServiceInter> loader = ServiceLoader.load(MyServiceInter.class);
        //所有的实现类
        for(MyServiceInter service:loader){
            service.method();
        }

//        MyServiceInterImpl.staticImpl(); ont中export必须是第一层包，不可以套多层

    }
}
