package com.java110.hardwareAdapation.dao;


import com.java110.utils.exception.DAOException;
import com.java110.entity.merchant.BoMerchant;
import com.java110.entity.merchant.BoMerchantAttr;
import com.java110.entity.merchant.Merchant;
import com.java110.entity.merchant.MerchantAttr;


import java.util.List;
import java.util.Map;

/**
 * 设备上报组件内部之间使用，没有给外围系统提供服务能力
 * 设备上报服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 *
 * Created by wuxw on 2016/12/27.
 */
public interface IMachineRecordServiceDao {

    /**
     * 保存 设备上报信息
     * @param businessMachineRecordInfo 设备上报信息 封装
     * @throws DAOException 操作数据库异常
     */
    void saveBusinessMachineRecordInfo(Map businessMachineRecordInfo) throws DAOException;



    /**
     * 查询设备上报信息（business过程）
     * 根据bId 查询设备上报信息
     * @param info bId 信息
     * @return 设备上报信息
     * @throws DAOException DAO异常
     */
    List<Map> getBusinessMachineRecordInfo(Map info) throws DAOException;




    /**
     * 保存 设备上报信息 Business数据到 Instance中
     * @param info
     * @throws DAOException DAO异常
     */
    void saveMachineRecordInfoInstance(Map info) throws DAOException;




    /**
     * 查询设备上报信息（instance过程）
     * 根据bId 查询设备上报信息
     * @param info bId 信息
     * @return 设备上报信息
     * @throws DAOException DAO异常
     */
    List<Map> getMachineRecordInfo(Map info) throws DAOException;



    /**
     * 修改设备上报信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    void updateMachineRecordInfoInstance(Map info) throws DAOException;


    /**
     * 查询设备上报总数
     *
     * @param info 设备上报信息
     * @return 设备上报数量
     */
    int queryMachineRecordsCount(Map info);

}
