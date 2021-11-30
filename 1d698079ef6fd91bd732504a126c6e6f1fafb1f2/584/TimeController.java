package com.hlj.redis.contollertimes;

import com.hlj.redis.contollertimes.annotation.EntryTimes;
import com.hlj.redis.lock.utils.DistributedLock;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/13  下午2:19.
 */
@RequestMapping("redis/time")
@Controller
public class TimeController {


    @GetMapping("time")
    @ResponseBody
    @EntryTimes
    public String time(){

        return "success";

    }


}