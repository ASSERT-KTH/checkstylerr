package com.hlj.moudle.demo.service.impl;

import com.hlj.dao.db.DemoEntityRepository;
import com.hlj.dao.mybatis.demo.DemoEntityMapper;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.demo.service.DemoEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
}
