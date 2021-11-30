package com.hlj.springboot.dome.common.moudle.service;

import com.hlj.springboot.dome.common.entity.DemoEntity;
import com.hlj.springboot.dome.common.entity.repository.DemoEntityRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/10/19  下午3:34.
 */
@Service
public class PostConfig {

    @Resource
    private DemoEntityRepository demoEntityRepository;

    public static DemoEntity demoEntity ;

    @PostConstruct
    public void afterProperties(){
        demoEntity= demoEntityRepository.findOne(1L);
    }
}
