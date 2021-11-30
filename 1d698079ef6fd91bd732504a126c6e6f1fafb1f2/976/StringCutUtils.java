package com.duodian.youhui.admin.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @Desc: String 切割工具
 * @Author HealerJean
 * @Date 2018/6/11  下午5:05.
 */
@Slf4j
public class StringCutUtils {



    /**
     * 获取各种参数
     * @param param id
     * @param values  1
     * @return
     */
    //server.healerjean.cn/id=1&fuId=121233
    public static String getParam(String param, String values){
        String[]  arrSplit=url.split("&");
        for(String strSplit:arrSplit){
            if(strSplit.contains(param+"=")){
                String []finalSplit =   strSplit.split("=");
                return ( finalSplit[1]);
            }
        }
        return null;
    }



}
