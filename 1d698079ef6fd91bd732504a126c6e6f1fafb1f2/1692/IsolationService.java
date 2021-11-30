package com.hlj.springboot.dome.common.moudle.service;

import com.hlj.springboot.dome.common.entity.DemoEntity;
import com.hlj.springboot.dome.common.entity.OtherEntity;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/23  下午7:34.
 * 类描述：
 */
public interface IsolationService {



    /**
     * 方法中的事物为入口方法的事物
     * @param id
     * @return
     */
    DemoEntity  transRequirs(Long id,String name);
    DemoEntity  transRequirsFind(Long id);

    /**
     * 开启一个事物
     * @param id
     * @return
     */
    DemoEntity  transRequirsNew (Long id,String name);


    /**
     * 事物隔离级别为 读已提交（）
     * @param id
     * @return
     */
    DemoEntity   isoLationReadCommitedFind(Long id) ;


    int updateName(Long id,String name);


    OtherEntity findOther(Long id);


    DemoEntity sqlFind(Long id) ;
}
