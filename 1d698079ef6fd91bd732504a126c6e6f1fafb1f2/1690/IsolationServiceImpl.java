package com.hlj.springboot.dome.common.moudle.service.impl;

import com.hlj.springboot.dome.common.entity.DemoEntity;
import com.hlj.springboot.dome.common.entity.OtherEntity;
import com.hlj.springboot.dome.common.entity.repository.DemoEntityRepository;
import com.hlj.springboot.dome.common.entity.repository.OtherEntityRepository;
import com.hlj.springboot.dome.common.moudle.service.IsolationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/23  下午7:35.
 * 类描述：
 */
@Service
@Slf4j
public class IsolationServiceImpl  implements IsolationService {

    @Resource
    private DemoEntityRepository demoEntityRepository ;

    @Resource
    private OtherEntityRepository otherEntityRepository ;


    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public DemoEntity transRequirs(Long id,String name) {
        DemoEntity demoEntity =  demoEntityRepository.findOne(id);
         demoEntity.setName(name);
        return demoEntityRepository.save(demoEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public DemoEntity transRequirsFind(Long id) {
        return demoEntityRepository.findOne(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    @Override
    public DemoEntity transRequirsNew(Long id,String name) {
        DemoEntity demoEntity =  demoEntityRepository.findOne(id);
        demoEntity.setName(name);
        return demoEntityRepository.save(demoEntity);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public DemoEntity isoLationReadCommitedFind(Long id) {
        return demoEntityRepository.findOne(id);
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public int updateName(Long id, String name) {
        return demoEntityRepository.updateName(id,name);
    }


    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public OtherEntity findOther(Long id) {
        return otherEntityRepository.findOne(id);
    }



    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    @Override
    public DemoEntity sqlFind(Long id) {
        return demoEntityRepository.sqlFind(id);
    }
}
