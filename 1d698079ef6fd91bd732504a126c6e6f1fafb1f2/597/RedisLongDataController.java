package com.hlj.redis.controller;

import com.hlj.common.Format.ResponseBean;
import com.hlj.redis.redisTool.RedisLongData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RedisLongDataController {


    @Autowired
    private RedisLongData redisLongData;

    /**
     * 添加Long类型的缓存数据
     */
    @RequestMapping("/setRedisLongData")
    public @ResponseBody
    ResponseBean setRedisLongData(Long id){
        try {
            redisLongData.set("long",id);
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

	/**
	 * 获取Long类型的缓存数据
	 */
	
    @RequestMapping("/getRedisLongData")
    public @ResponseBody ResponseBean getRedisLongData(String key){
        try {
            Long id = redisLongData.get(key);
            return ResponseBean.buildSuccess(id);
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

}
