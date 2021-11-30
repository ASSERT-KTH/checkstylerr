package com.hlj.moudle.staticT.service.impl;

import com.hlj.moudle.staticT.service.StaticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j //下面这些变量与scope的状态有关，主要是看它有没有成员变量 ，默认是single
public class StaticServiceImpl implements StaticService {

    public static int static_n = 1 ;
    public  int no_static_n  = 1 ;

    public String addNostaticN(){
        ++no_static_n ;
        return "非静态"+no_static_n;
    }

    public String addStaticN(){
        ++static_n ;
        return "静态"+static_n;
    }

}
