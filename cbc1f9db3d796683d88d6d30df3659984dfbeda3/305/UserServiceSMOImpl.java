package com.java110.user.smo.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.KafkaConstant;
import com.java110.utils.constant.MappingConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.SMOException;
import com.java110.utils.kafka.KafkaFactory;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.core.context.BusinessServiceDataFlow;
import com.java110.core.factory.DataFlowFactory;
import com.java110.entity.center.DataFlowLinksCost;
import com.java110.entity.center.DataFlowLog;
import com.java110.event.service.BusinessServiceDataFlowEventPublishing;
import com.java110.user.dao.IUserServiceDao;
import com.java110.user.smo.IUserServiceSMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 用户服务信息管理业务信息实现
 * Created by wuxw on 2017/4/5.
 */
@Service("userServiceSMOImpl")
@Transactional
public class UserServiceSMOImpl extends BaseServiceSMO implements IUserServiceSMO {

    private  static Logger logger = LoggerFactory.getLogger(UserServiceSMOImpl.class);


    @Autowired
    IUserServiceDao iUserServiceDao;

    //新增用户
    private static final String USER_ACTION_ADD = "ADD";

    //新增用户
    private static final String USER_ACTION_KIP = "KIP";

    //新增用户
    private static final String USER_ACTION_DEL = "DEL";


    @Override
    public JSONObject service(BusinessServiceDataFlow businessServiceDataFlow) throws SMOException {
        try {
            Assert.hasLength(businessServiceDataFlow.getbId(), "bId 不能为空");

            BusinessServiceDataFlowEventPublishing.multicastEvent(businessServiceDataFlow);
            Assert.notEmpty(businessServiceDataFlow.getResJson(), "用户服务[" + businessServiceDataFlow.getCurrentBusiness().getServiceCode() + "]没有返回内容");
        } catch (Exception e) {
            logger.error("用户信息处理异常", e);
            throw new SMOException(ResponseConstant.RESULT_PARAM_ERROR, "用户信息处理异常" + e.getMessage());
        } finally {
            if (businessServiceDataFlow == null) {
                return null;
            }

            //这里记录日志
            Date endDate = DateUtil.getCurrentDate();

            businessServiceDataFlow.setEndDate(endDate);
            //添加耗时
            DataFlowFactory.addCostTime(businessServiceDataFlow, "service", "业务处理总耗时",
                    businessServiceDataFlow.getStartDate(), businessServiceDataFlow.getEndDate());
            //保存耗时
            saveCostTimeLogMessage(businessServiceDataFlow);
            //保存日志
            saveLogMessage(businessServiceDataFlow);
        }
        return businessServiceDataFlow.getResJson();
    }


    /**
     * 保存日志信息
     *
     * @param businessServiceDataFlow 业务日志对象
     */
    private void saveLogMessage(BusinessServiceDataFlow businessServiceDataFlow) {

        try {
            if (MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_LOG_ON_OFF))) {
                for (DataFlowLog dataFlowLog : businessServiceDataFlow.getLogDatas()) {
                    KafkaFactory.sendKafkaMessage(KafkaConstant.TOPIC_LOG_NAME, "", JSONObject.toJSONString(dataFlowLog));
                }
            }
        } catch (Exception e) {
            logger.error("报错日志出错了，", e);
        }
    }

    /**
     * 保存耗时信息
     *
     * @param businessServiceDataFlow
     */
    private void saveCostTimeLogMessage(BusinessServiceDataFlow businessServiceDataFlow) {
        try {
            if (MappingConstant.VALUE_ON.equals(MappingCache.getValue(MappingConstant.KEY_COST_TIME_ON_OFF))) {
                List<DataFlowLinksCost> dataFlowLinksCosts = businessServiceDataFlow.getLinksCostDates();
                JSONObject costDate = new JSONObject();
                JSONArray costDates = new JSONArray();
                JSONObject newObj = null;
                for (DataFlowLinksCost dataFlowLinksCost : dataFlowLinksCosts) {
                    newObj = JSONObject.parseObject(JSONObject.toJSONString(dataFlowLinksCost));
                    newObj.put("dataFlowId", businessServiceDataFlow.getDataFlowId());
                    newObj.put("transactionId", businessServiceDataFlow.getTransactionId());
                    costDates.add(newObj);
                }
                costDate.put("costDates", costDates);

                KafkaFactory.sendKafkaMessage(KafkaConstant.TOPIC_COST_TIME_LOG_NAME, "", costDate.toJSONString());
            }
        } catch (Exception e) {
            logger.error("报错日志出错了，", e);
        }
    }


    public IUserServiceDao getiUserServiceDao() {
        return iUserServiceDao;
    }

    public void setiUserServiceDao(IUserServiceDao iUserServiceDao) {
        this.iUserServiceDao = iUserServiceDao;
    }
}
