package com.hlj.springboot.dome.common.moudle.service.impl;

import com.hlj.springboot.dome.common.entity.repository.DemoEntityRepository;
import com.hlj.springboot.dome.common.moudle.service.DemoEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class DemoEntityServiceImpl implements DemoEntityService {

    @Resource
    DemoEntityRepository demoEntityRepository ;



}
