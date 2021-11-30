package com.hlj.many.datasourse.dataresource.service;

import com.hlj.many.datasourse.data.DataSource;
import com.hlj.many.datasourse.data.TargetDataSource;
import com.hlj.many.datasourse.dataresource.dao.db.PersonOneDao;
import com.hlj.many.datasourse.dataresource.dao.db.PersonOneMapper;
import com.hlj.many.datasourse.dataresource.dao.db.PersonTwoDao;
import com.hlj.many.datasourse.dataresource.dao.db.entry.PersonOne;
import com.hlj.many.datasourse.dataresource.dao.db.entry.PersonTwo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/19  下午1:58.
 */
@Service
@TargetDataSource(DataSource.ONE)
public class OneService {

    @Resource
    private PersonOneDao personOneDao;

    @Resource
    private PersonOneMapper personOneMapper;

    @Resource
    private PersonTwoDao personTwoDao ;

    @Resource
    private TwoService twoService ;


    public PersonTwo save(PersonOne personOne){
        PersonTwo personTwo = new PersonTwo();
        personTwo.setName(personOne.getName());
        return personTwoDao.save(personTwo);
    }

    public PersonOne findById(Long id){
       return personOneMapper.findById(id);
    }


    public PersonTwo onetoTwo(PersonOne personOne){
        PersonTwo personTwo = new PersonTwo();
        personTwo.setName(personOne.getName());
        return twoService.save(personTwo);
    }



}
