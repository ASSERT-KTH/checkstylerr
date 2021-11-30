package com.hlj.moudle.produce.service;


import com.hlj.entity.db.demo.DemoEntity;

import java.util.List;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
public interface ProcedureService {


    /**
     * 1、获取 存储过程out参数值
     * @param id
     * @return
     */
    Integer procedureGetOut(Long id);

    /**
     * 2、获取 存储过程的 获取多个集合 使用了泛型，有可能集合中是不同的
     * @param
     * @return
     */
    List<DemoEntity> procedureGetOneList(String name);


    /**
     * 3、获取 存储过程的 获取多个集合 使用了泛型，有可能集合中是不同的
     * @param
     * @return
     */
    List<List<?>> procedureGetManyList(String oneName,String twoName);





//    JpA调用 - JpA调用  JpA调用 - JpA调用  JpA调用 - JpA调用  JpA调用 - JpA调用




    /**
     * 1、获取 存储过程out参数值
     * @param id
     * @return
     */
    Integer jpaProcedureGetOut(Long id);

    /**
     * 2、获取 存储过程的 获取多个集合 使用了泛型，有可能集合中是不同的
     * @param
     * @return
     */
    List<DemoEntity> jpaProcedureGetOneList(String name);


    /**
     * 3、获取 存储过程的 获取多个集合 使用了泛型，有可能集合中是不同的
     * @param
     * @return
     */
    List<List<?>> jpaProcedureGetManyList(String oneName,String twoName);






}
