package com.java110.common.dao;


import com.java110.utils.exception.DAOException;
import com.java110.entity.merchant.BoMerchant;
import com.java110.entity.merchant.BoMerchantAttr;
import com.java110.entity.merchant.Merchant;
import com.java110.entity.merchant.MerchantAttr;


import java.util.List;
import java.util.Map;

/**
 * 消息组件内部之间使用，没有给外围系统提供服务能力
 * 消息服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 *
 * Created by wuxw on 2016/12/27.
 */
public interface IMsgServiceDao {

    /**
     * 保存 消息信息
     * @param businessMsgInfo 消息信息 封装
     * @throws DAOException 操作数据库异常
     */
    void saveBusinessMsgInfo(Map businessMsgInfo) throws DAOException;



    /**
     * 查询消息信息（business过程）
     * 根据bId 查询消息信息
     * @param info bId 信息
     * @return 消息信息
     * @throws DAOException DAO异常
     */
    List<Map> getBusinessMsgInfo(Map info) throws DAOException;




    /**
     * 保存 消息信息 Business数据到 Instance中
     * @param info
     * @throws DAOException DAO异常
     */
    void saveMsgInfoInstance(Map info) throws DAOException;




    /**
     * 查询消息信息（instance过程）
     * 根据bId 查询消息信息
     * @param info bId 信息
     * @return 消息信息
     * @throws DAOException DAO异常
     */
    List<Map> getMsgInfo(Map info) throws DAOException;



    /**
     * 修改消息信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    void updateMsgInfoInstance(Map info) throws DAOException;


    /**
     * 查询消息总数
     *
     * @param info 消息信息
     * @return 消息数量
     */
    int queryMsgsCount(Map info);

}
