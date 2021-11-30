package com.hlj.netty.websocket.executor;


import com.hlj.netty.websocket.bean.ConvertBean;
import com.hlj.netty.websocket.bean.RequestBean;
import com.hlj.netty.websocket.helper.SpringHelper;
import com.hlj.netty.websocket.selector.ClientChannelRelation;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by j.sh on 27/11/2017.
 */
public class AsyncExecutor {

    private static Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);

    private static ExecutorService pool = Executors.newFixedThreadPool(1);
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static LinkedBlockingQueue<RequestBean> blockingQueue = new LinkedBlockingQueue<>();


    public static void offerQueue(RequestBean requestBean){
        blockingQueue.offer(requestBean); //能放就放，不能放拉倒
    }

    public static void start(){
        pool.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(" pool.execute(new Runnable() {");
                while (true) {
                    try {
                        final RequestBean requestBean = blockingQueue.take(); //脾气好，一直等东西
                        executorService.execute(new Runnable() {

                            @Override
                            public void run() {
                                System.out.println("线程的id我主要观察这里的线程有没有发生变化"+Thread.currentThread().getId());
                                System.out.println(" executorService.execute(new Runnable() {");
                                try {
                                    RedisTemplate redisTemplate = (RedisTemplate) SpringHelper.getBeanByName("redisTemplate");
                                    JSONObject jsonObject = JSONObject.fromObject(requestBean.getRequest());

                                    System.out.println(requestBean.getRequest());
                    //初始化状态，添加通道
                                    if (jsonObject.containsKey("init")) {
                                        //init
                                        String uid = jsonObject.getString("uid");
                                        ClientChannelRelation.addRelation(uid,requestBean.getChannel());
                                    }
                     //添加通道，对话
                                    else {
                                        //content
                                        String toUid = jsonObject.getString("to");
                                        String content = jsonObject.getString("content");

                                        redisTemplate.convertAndSend("request",new ConvertBean(toUid,content));
                                        requestBean.getPromise().setSuccess("");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    requestBean.getPromise().setFailure(e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        });
    }
}
