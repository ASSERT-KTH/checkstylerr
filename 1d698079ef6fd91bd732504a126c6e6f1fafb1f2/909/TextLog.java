package com.hlj.proj.utils;

import com.hlj.proj.controler.Log4jController;
import org.apache.log4j.Logger;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName TextLog
 * @date 2019-07-13  10:30.
 * @Description
 */
public class TextLog {



    public static void main(String[] args) {
        Logger log = Logger.getLogger(Log4jController.class);
        for(int i=0;i<=5000;i++){
            System.out.println("循环"+"--"+i);
            try{
                System.out.println(1%0);
            }catch(Exception e){
                log.error("测试日志"+"--"+"异常信息"+i+"："+e);
            }
        }
    }


}
