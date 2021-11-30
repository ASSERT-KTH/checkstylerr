package com.hlj.many.datasourse.dataresource.service;

import com.hlj.many.datasourse.dataresource.dao.data.PersonOneDao;
import com.hlj.many.datasourse.dataresource.dao.data.entry.PersonOne;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/19  下午1:58.
 */
@Service
public class MainService {

    @Resource
    private PersonOneDao personOneDao;



    /**
     * 默认选择第一种数据源
     * @param personOne
     * @return
     */
    public PersonOne save(PersonOne personOne){
       return personOneDao.save(personOne);
    }






    
}
