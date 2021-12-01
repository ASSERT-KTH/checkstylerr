package com.java110.common.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.DAOException;
import com.java110.utils.util.DateUtil;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.common.dao.IAttrValueServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 属性值服务 与数据库交互
 * Created by wuxw on 2017/4/5.
 */
@Service("attrValueServiceDaoImpl")
//@Transactional
public class AttrValueServiceDaoImpl extends BaseServiceDao implements IAttrValueServiceDao {

    private static Logger logger = LoggerFactory.getLogger(AttrValueServiceDaoImpl.class);





    /**
     * 保存属性值信息 到 instance
     * @param info   bId 信息
     * @throws DAOException DAO异常
     */
    @Override
    public void saveAttrValueInfo(Map info) throws DAOException {
        logger.debug("保存属性值信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.insert("attrValueServiceDaoImpl.saveAttrValueInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"保存属性值信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }


    /**
     * 查询属性值信息（instance）
     * @param info bId 信息
     * @return List<Map>
     * @throws DAOException DAO异常
     */
    @Override
    public List<Map> getAttrValueInfo(Map info) throws DAOException {
        logger.debug("查询属性值信息 入参 info : {}",info);

        List<Map> businessAttrValueInfos = sqlSessionTemplate.selectList("attrValueServiceDaoImpl.getAttrValueInfo",info);

        return businessAttrValueInfos;
    }


    /**
     * 修改属性值信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    @Override
    public void updateAttrValueInfo(Map info) throws DAOException {
        logger.debug("修改属性值信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.update("attrValueServiceDaoImpl.updateAttrValueInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"修改属性值信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }

     /**
     * 查询属性值数量
     * @param info 属性值信息
     * @return 属性值数量
     */
    @Override
    public int queryAttrValuesCount(Map info) {
        logger.debug("查询属性值数据 入参 info : {}",info);

        List<Map> businessAttrValueInfos = sqlSessionTemplate.selectList("attrValueServiceDaoImpl.queryAttrValuesCount", info);
        if (businessAttrValueInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessAttrValueInfos.get(0).get("count").toString());
    }


}
