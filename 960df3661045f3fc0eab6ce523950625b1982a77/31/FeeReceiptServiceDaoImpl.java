package com.java110.fee.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.DAOException;
import com.java110.utils.util.DateUtil;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.fee.dao.IFeeReceiptServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 收据服务 与数据库交互
 * Created by wuxw on 2017/4/5.
 */
@Service("feeReceiptServiceDaoImpl")
//@Transactional
public class FeeReceiptServiceDaoImpl extends BaseServiceDao implements IFeeReceiptServiceDao {

    private static Logger logger = LoggerFactory.getLogger(FeeReceiptServiceDaoImpl.class);





    /**
     * 保存收据信息 到 instance
     * @param info   bId 信息
     * @throws DAOException DAO异常
     */
    @Override
    public void saveFeeReceiptInfo(Map info) throws DAOException {
        logger.debug("保存收据信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.insert("feeReceiptServiceDaoImpl.saveFeeReceiptInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"保存收据信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }


    /**
     * 查询收据信息（instance）
     * @param info bId 信息
     * @return List<Map>
     * @throws DAOException DAO异常
     */
    @Override
    public List<Map> getFeeReceiptInfo(Map info) throws DAOException {
        logger.debug("查询收据信息 入参 info : {}",info);

        List<Map> businessFeeReceiptInfos = sqlSessionTemplate.selectList("feeReceiptServiceDaoImpl.getFeeReceiptInfo",info);

        return businessFeeReceiptInfos;
    }


    /**
     * 修改收据信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    @Override
    public void updateFeeReceiptInfo(Map info) throws DAOException {
        logger.debug("修改收据信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.update("feeReceiptServiceDaoImpl.updateFeeReceiptInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"修改收据信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }

     /**
     * 查询收据数量
     * @param info 收据信息
     * @return 收据数量
     */
    @Override
    public int queryFeeReceiptsCount(Map info) {
        logger.debug("查询收据数据 入参 info : {}",info);

        List<Map> businessFeeReceiptInfos = sqlSessionTemplate.selectList("feeReceiptServiceDaoImpl.queryFeeReceiptsCount", info);
        if (businessFeeReceiptInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessFeeReceiptInfos.get(0).get("count").toString());
    }


}
