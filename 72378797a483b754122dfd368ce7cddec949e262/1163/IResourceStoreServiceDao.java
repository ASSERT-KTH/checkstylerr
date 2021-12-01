package com.java110.store.dao;


import com.java110.utils.exception.DAOException;
import com.java110.entity.merchant.BoMerchant;
import com.java110.entity.merchant.BoMerchantAttr;
import com.java110.entity.merchant.Merchant;
import com.java110.entity.merchant.MerchantAttr;


import java.util.List;
import java.util.Map;

/**
 * 资源组件内部之间使用，没有给外围系统提供服务能力
 * 资源服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 *
 * Created by wuxw on 2016/12/27.
 */
public interface IResourceStoreServiceDao {

    /**
     * 保存 资源信息
     * @param businessResourceStoreInfo 资源信息 封装
     * @throws DAOException 操作数据库异常
     */
    void saveBusinessResourceStoreInfo(Map businessResourceStoreInfo) throws DAOException;



    /**
     * 查询资源信息（business过程）
     * 根据bId 查询资源信息
     * @param info bId 信息
     * @return 资源信息
     * @throws DAOException DAO异常
     */
    List<Map> getBusinessResourceStoreInfo(Map info) throws DAOException;




    /**
     * 保存 资源信息 Business数据到 Instance中
     * @param info
     * @throws DAOException DAO异常
     */
    void saveResourceStoreInfoInstance(Map info) throws DAOException;




    /**
     * 查询资源信息（instance过程）
     * 根据bId 查询资源信息
     * @param info bId 信息
     * @return 资源信息
     * @throws DAOException DAO异常
     */
    List<Map> getResourceStoreInfo(Map info) throws DAOException;



    /**
     * 修改资源信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    void updateResourceStoreInfoInstance(Map info) throws DAOException;


    /**
     * 查询资源总数
     *
     * @param info 资源信息
     * @return 资源数量
     */
    int queryResourceStoresCount(Map info);

}
