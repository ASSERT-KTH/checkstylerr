package com.hlj.moudle.cache.service.impl;

import com.hlj.dao.db.DemoEntityRepository;
import com.hlj.dao.mybatis.demo.DemoEntityMapper;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.cache.config.EhCacheObjectData;
import com.hlj.moudle.cache.service.HibernateCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class HibernateCacheServiceImpl implements HibernateCacheService {

    @Resource
    private DemoEntityMapper demoEntityMapper ;

    @Resource
    private DemoEntityRepository demoEntityRepository ;



    @Override
    public DemoEntity addDemoEntity(DemoEntity demoEntity) {
        return demoEntityRepository.save(demoEntity);
    }

    @Override
    public DemoEntity findById(Long id) {
        return demoEntityMapper.findById(id);
    }

    @Override
    public List<DemoEntity> findAll() {
        List<DemoEntity> demoEntities = demoEntityMapper.findAll();
        return demoEntities;
    }


    /**
     * hibernate 一级缓存
     * hibernate的一级缓存是session级别的，所以如果session关闭后，缓存就没了，此时就会再次发sql去查数据库。
     * 作用范围较小！ 缓存的事件短。
     * 缓存效果不明显
     *
     * @param id
     */
    public void oneCache(Long id){

        DemoEntity demoEntity = demoEntityRepository.findOne(id);
        System.out.println(demoEntity);
        //Hibernate: select demoentity0_.id as id1_0_0_, demoentity0_.age as age2_0_0_, demoentity0_.cdate as cdate3_0_0_, demoentity0_.name as name4_0_0_, demoentity0_.udate as udate5_0_0_ from demo_entity demoentity0_ where demoentity0_.id=?

        //用到了缓存,没有执行sql语句
        DemoEntity demoEntity12 = demoEntityRepository.findOne(id);
        System.out.println(demoEntity12);

        //因为是sql的形式，所以没有用到缓存
        DemoEntity sqlEntity13 = demoEntityRepository.testCachefindById(id);
        System.out.println(sqlEntity13);
        //Hibernate: select demoentity0_.id as id1_0_, demoentity0_.age as age2_0_, demoentity0_.cdate as cdate3_0_, demoentity0_.name as name4_0_, demoentity0_.udate as udate5_0_ from demo_entity demoentity0_ where demoentity0_.id=?
    }



    /**
     * 更新缓存
     * @param id
     */
    public void updateOneCache(Long id){

        DemoEntity demoEntity = demoEntityRepository.findOne(id);

        demoEntity.setName("更新"+new Date().toString());
        demoEntityRepository.save(demoEntity);

        //用到了缓存,没有执行sql语句
        DemoEntity demoEntity12 = demoEntityRepository.findOne(id);
        System.out.println(demoEntity12);

        System.out.println("---------");

    }









    /**
     * 二级缓存
     * Hibernate提供了基于应用程序级别的缓存， 可以跨多个session，即不同的session都可以访问缓存数据。 这个换存也叫二级缓存。
     *
     * ibernate提供的二级缓存有默认的实现，且是一种可插配的缓存框架！
     */
    public void twoCache(Long id){

        DemoEntity demoEntity = demoEntityRepository.findOne(id);
        System.out.println(demoEntity);

        DemoEntity demoEntity12 = demoEntityRepository.findOne(id);
        System.out.println(demoEntity12);

        DemoEntity sqlEntity13 = demoEntityRepository.testCachefindById(id);
        System.out.println(sqlEntity13);
    }

}
