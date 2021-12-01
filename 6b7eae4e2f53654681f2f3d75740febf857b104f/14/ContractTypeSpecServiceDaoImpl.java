package com.java110.store.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.DAOException;
import com.java110.utils.util.DateUtil;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.store.dao.IContractTypeSpecServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 合同类型规格服务 与数据库交互
 * Created by wuxw on 2017/4/5.
 */
@Service("contractTypeSpecServiceDaoImpl")
//@Transactional
public class ContractTypeSpecServiceDaoImpl extends BaseServiceDao implements IContractTypeSpecServiceDao {

    private static Logger logger = LoggerFactory.getLogger(ContractTypeSpecServiceDaoImpl.class);





    /**
     * 保存合同类型规格信息 到 instance
     * @param info   bId 信息
     * @throws DAOException DAO异常
     */
    @Override
    public void saveContractTypeSpecInfo(Map info) throws DAOException {
        logger.debug("保存合同类型规格信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.insert("contractTypeSpecServiceDaoImpl.saveContractTypeSpecInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"保存合同类型规格信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }


    /**
     * 查询合同类型规格信息（instance）
     * @param info bId 信息
     * @return List<Map>
     * @throws DAOException DAO异常
     */
    @Override
    public List<Map> getContractTypeSpecInfo(Map info) throws DAOException {
        logger.debug("查询合同类型规格信息 入参 info : {}",info);

        List<Map> businessContractTypeSpecInfos = sqlSessionTemplate.selectList("contractTypeSpecServiceDaoImpl.getContractTypeSpecInfo",info);

        return businessContractTypeSpecInfos;
    }


    /**
     * 修改合同类型规格信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    @Override
    public void updateContractTypeSpecInfo(Map info) throws DAOException {
        logger.debug("修改合同类型规格信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.update("contractTypeSpecServiceDaoImpl.updateContractTypeSpecInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"修改合同类型规格信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }

     /**
     * 查询合同类型规格数量
     * @param info 合同类型规格信息
     * @return 合同类型规格数量
     */
    @Override
    public int queryContractTypeSpecsCount(Map info) {
        logger.debug("查询合同类型规格数据 入参 info : {}",info);

        List<Map> businessContractTypeSpecInfos = sqlSessionTemplate.selectList("contractTypeSpecServiceDaoImpl.queryContractTypeSpecsCount", info);
        if (businessContractTypeSpecInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessContractTypeSpecInfos.get(0).get("count").toString());
    }


}
