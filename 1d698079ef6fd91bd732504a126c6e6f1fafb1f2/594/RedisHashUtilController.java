package com.hlj.redis.controller.tools;

import com.hlj.redis.project.data.ProjectData;
import com.hlj.redis.project.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 作者 ：HealerJean
 * 日期 ：2018/12/3  上午10:17.
 * 类描述：
 */
@RestController
public class RedisHashUtilController {

    Logger logger = LoggerFactory.getLogger(RedisHashUtilController.class);

    @Resource
    private RedisTemplate<String,Object> redisWithTemplate ;


    @Resource
    private StringRedisTemplate stringRedisTemplate ;


    //下面肯定会报错哦
    @GetMapping("hashPut")
    public ProjectData hashPut(){

        ProjectData projectData = new ProjectData().setName("HeaelerJean").setGroup("GROUP");
        redisWithTemplate.opsForHash().put("hashKey", 1000000L, projectData); //保存之后，乱码了，建议使用String类型保存

        //下面这个hashKey必须是字符串，所以是错误的
//        stringRedisTemplate.opsForHash().put("stringhashKey", 1000000L, JsonUtils.toJson(projectData));
        return  projectData ;
    }

    //下面肯定会报错哦
    @GetMapping("hashGet")
    public ProjectData hashGet(){
        Long lo = 1000000L ;
        ProjectData projectData = (ProjectData)  redisWithTemplate.opsForHash().get("hashKey", lo) ;
        logger.info(projectData.toString());

//        ProjectData stringProject = JsonUtils.toObject( stringRedisTemplate.opsForHash().get("stringhashKey", lo).toString(),ProjectData.class);
//        logger.info(stringProject.toString());

        return  projectData ;
    }



}
