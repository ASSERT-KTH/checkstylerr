package com.hlj.moudle.demo.service.impl;

import com.hlj.dao.db.FatherRepository;
import com.hlj.dao.mybatis.demo.FatherMapper;
import com.hlj.dao.mybatis.demo.SonMapper;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.demo.service.DemoEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class DemoEntityServiceImpl implements DemoEntityService {

    @Resource
    private FatherMapper fatherMapper;

    @Resource
    private SonMapper sonMapper;


    @Override
    public List<DemoEntity> mapperExtend() {
        List<DemoEntity> demoEntities = new ArrayList<>();
        demoEntities.add(fatherMapper.findById1());
        demoEntities.add(fatherMapper.extendMethod());

        demoEntities.add(sonMapper.findById1()); //继承的父类
        demoEntities.add(sonMapper.extendMethod());//重写的方法
        demoEntities.add(sonMapper.findById3());//自己的方法

        return demoEntities;
    }
}


