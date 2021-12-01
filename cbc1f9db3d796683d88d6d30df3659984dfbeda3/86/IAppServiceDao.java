package com.java110.common.dao;


import com.java110.utils.exception.DAOException;

import java.util.List;
import java.util.Map;

/**
 * 应用组件内部之间使用，没有给外围系统提供服务能力
 * 应用服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 *
 * Created by wuxw on 2016/12/27.
 */
public interface IAppServiceDao {

    /**
     * 保存 应用信息
     * @param businessAppInfo 应用信息 封装
     * @throws DAOException 操作数据库异常
     */
    int saveAppInfo(Map businessAppInfo) throws DAOException;



    /**
     * 查询应用信息（business过程）
     * 根据bId 查询应用信息
     * @param info bId 信息
     * @return 应用信息
     * @throws DAOException DAO异常
     */
    List<Map> getBusinessAppInfo(Map info) throws DAOException;




    /**
     * 保存 应用信息 Business数据到 Instance中
     * @param info
     * @throws DAOException DAO异常
     */
    void saveAppInfoInstance(Map info) throws DAOException;




    /**
     * 查询应用信息（instance过程）
     * 根据bId 查询应用信息
     * @param info bId 信息
     * @return 应用信息
     * @throws DAOException DAO异常
     */
    List<Map> getAppInfo(Map info) throws DAOException;



    /**
     * 修改应用信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    int updateAppInfo(Map info) throws DAOException;


    /**
     * 查询应用总数
     *
     * @param info 应用信息
     * @return 应用数量
     */
    int queryAppsCount(Map info);

}
