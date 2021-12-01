package com.java110.fee.dao;


import com.java110.utils.exception.DAOException;
import com.java110.entity.merchant.BoMerchant;
import com.java110.entity.merchant.BoMerchantAttr;
import com.java110.entity.merchant.Merchant;
import com.java110.entity.merchant.MerchantAttr;


import java.util.List;
import java.util.Map;

/**
 * 费用公式组件内部之间使用，没有给外围系统提供服务能力
 * 费用公式服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 *
 * Created by wuxw on 2016/12/27.
 */
public interface IFeeFormulaServiceDao {


    /**
     * 保存 费用公式信息
     * @param info
     * @throws DAOException DAO异常
     */
    void saveFeeFormulaInfo(Map info) throws DAOException;




    /**
     * 查询费用公式信息（instance过程）
     * 根据bId 查询费用公式信息
     * @param info bId 信息
     * @return 费用公式信息
     * @throws DAOException DAO异常
     */
    List<Map> getFeeFormulaInfo(Map info) throws DAOException;



    /**
     * 修改费用公式信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    void updateFeeFormulaInfo(Map info) throws DAOException;


    /**
     * 查询费用公式总数
     *
     * @param info 费用公式信息
     * @return 费用公式数量
     */
    int queryFeeFormulasCount(Map info);

}
