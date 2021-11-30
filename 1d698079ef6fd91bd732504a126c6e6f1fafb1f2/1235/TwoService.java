package com.hlj.many.datasourse.dataresource.service;

import com.hlj.many.datasourse.dataresource.dao.data.PersonTwoDao;
import com.hlj.many.datasourse.dataresource.dao.data.PersonTwoMapper;
import com.hlj.many.datasourse.dataresource.dao.data.entry.PersonTwo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/19  下午1:58.
 */
@Service
@Transactional(rollbackFor = Exception.class,transactionManager = "admoreDataTM")
public class TwoService {


    @Resource
     private PersonTwoDao personTwoDao;


    @Resource
    private PersonTwoMapper personTwoMapper;

    public PersonTwo save(PersonTwo personTwo){
        return personTwoDao.save(personTwo);
    }

    public PersonTwo findById(Long id){
        return personTwoMapper.findById(id);
    }


    public List<PersonTwo> findALL(){
        return personTwoMapper.findALL();
    }

}
