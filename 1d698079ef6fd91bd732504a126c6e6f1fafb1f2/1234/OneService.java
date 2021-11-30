package com.hlj.many.datasourse.dataresource.service;

import com.hlj.many.datasourse.dataresource.dao.data.PersonOneDao;
import com.hlj.many.datasourse.dataresource.dao.data.PersonOneMapper;
import com.hlj.many.datasourse.dataresource.dao.data.entry.PersonOne;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/19  下午1:58.
 */
@Service
@Transactional(rollbackFor = Exception.class,transactionManager = "admoreTM")
public class OneService {

    @Resource
    private PersonOneDao personOneDao;

    @Resource
    private PersonOneMapper personOneMapper;

    public PersonOne save(PersonOne personOne){
        return personOneDao.save(personOne);
    }

    public PersonOne findById(Long id){
       return personOneMapper.findById(id);
    }




}
