package com.hlj.many.datasourse.dataresource.service;

import com.hlj.many.datasourse.data.DataSource;
import com.hlj.many.datasourse.data.TargetDataSource;
import com.hlj.many.datasourse.dataresource.dao.db.PersonTwoDao;
import com.hlj.many.datasourse.dataresource.dao.db.PersonTwoMapper;
import com.hlj.many.datasourse.dataresource.dao.db.entry.PersonTwo;
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
@TargetDataSource(DataSource.TWO)
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
