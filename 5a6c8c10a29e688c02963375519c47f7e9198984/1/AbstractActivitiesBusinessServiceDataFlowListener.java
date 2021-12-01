package com.java110.community.listener.activities;

import com.alibaba.fastjson.JSONObject;
import com.java110.community.dao.IActivitiesServiceDao;
import com.java110.entity.center.Business;
import com.java110.event.service.AbstractBusinessServiceDataFlowListener;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.exception.ListenerExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动 服务侦听 父类
 * Created by wuxw on 2018/7/4.
 */
public abstract class AbstractActivitiesBusinessServiceDataFlowListener extends AbstractBusinessServiceDataFlowListener {
    private static Logger logger = LoggerFactory.getLogger(AbstractActivitiesBusinessServiceDataFlowListener.class);


    /**
     * 获取 DAO工具类
     *
     * @return
     */
    public abstract IActivitiesServiceDao getActivitiesServiceDaoImpl();

    /**
     * 刷新 businessActivitiesInfo 数据
     * 主要将 数据库 中字段和 接口传递字段建立关系
     *
     * @param businessActivitiesInfo
     */
    protected void flushBusinessActivitiesInfo(Map businessActivitiesInfo, String statusCd) {
        businessActivitiesInfo.put("newBId", businessActivitiesInfo.get("b_id"));
        businessActivitiesInfo.put("collectCount", businessActivitiesInfo.get("collect_count"));
        businessActivitiesInfo.put("likeCount", businessActivitiesInfo.get("like_count"));
        businessActivitiesInfo.put("title", businessActivitiesInfo.get("title"));
        businessActivitiesInfo.put("readCount", businessActivitiesInfo.get("read_count"));
        businessActivitiesInfo.put("userName", businessActivitiesInfo.get("user_name"));
        businessActivitiesInfo.put("userId", businessActivitiesInfo.get("user_id"));
        businessActivitiesInfo.put("activitiesId", businessActivitiesInfo.get("activities_id"));
        businessActivitiesInfo.put("operate", businessActivitiesInfo.get("operate"));
        businessActivitiesInfo.put("typeCd", businessActivitiesInfo.get("type_cd"));
        businessActivitiesInfo.put("context", businessActivitiesInfo.get("context"));
        businessActivitiesInfo.put("startTime", businessActivitiesInfo.get("start_time"));
        businessActivitiesInfo.put("endTime", businessActivitiesInfo.get("end_time"));
        businessActivitiesInfo.put("communityId", businessActivitiesInfo.get("community_id"));
        businessActivitiesInfo.put("headerImg", businessActivitiesInfo.get("header_img"));
        businessActivitiesInfo.remove("bId");
        businessActivitiesInfo.put("statusCd", statusCd);
    }


    /**
     * 当修改数据时，查询instance表中的数据 自动保存删除数据到business中
     *
     * @param businessActivities 活动信息
     */
    protected void autoSaveDelBusinessActivities(Business business, JSONObject businessActivities) {
//自动插入DEL
        Map info = new HashMap();
        info.put("activitiesId", businessActivities.getString("activitiesId"));
        info.put("statusCd", StatusConstant.STATUS_CD_VALID);
        List<Map> currentActivitiesInfos = getActivitiesServiceDaoImpl().getActivitiesInfo(info);
        if (currentActivitiesInfos == null || currentActivitiesInfos.size() != 1) {
            throw new ListenerExecuteException(ResponseConstant.RESULT_PARAM_ERROR, "未找到需要修改数据信息，入参错误或数据有问题，请检查" + info);
        }

        Map currentActivitiesInfo = currentActivitiesInfos.get(0);

        currentActivitiesInfo.put("bId", business.getbId());

        currentActivitiesInfo.put("collectCount", currentActivitiesInfo.get("collect_count"));
        currentActivitiesInfo.put("likeCount", currentActivitiesInfo.get("like_count"));
        currentActivitiesInfo.put("title", currentActivitiesInfo.get("title"));
        currentActivitiesInfo.put("readCount", currentActivitiesInfo.get("read_count"));
        currentActivitiesInfo.put("userName", currentActivitiesInfo.get("user_name"));
        currentActivitiesInfo.put("userId", currentActivitiesInfo.get("user_id"));
        currentActivitiesInfo.put("activitiesId", currentActivitiesInfo.get("activities_id"));
        currentActivitiesInfo.put("operate", currentActivitiesInfo.get("operate"));
        currentActivitiesInfo.put("typeCd", currentActivitiesInfo.get("type_cd"));
        currentActivitiesInfo.put("context", currentActivitiesInfo.get("context"));
        currentActivitiesInfo.put("startTime", currentActivitiesInfo.get("start_time"));
        currentActivitiesInfo.put("endTime", currentActivitiesInfo.get("end_time"));
        currentActivitiesInfo.put("communityId", currentActivitiesInfo.get("community_id"));
        currentActivitiesInfo.put("headerImg", currentActivitiesInfo.get("header_img"));


        currentActivitiesInfo.put("operate", StatusConstant.OPERATE_DEL);
        getActivitiesServiceDaoImpl().saveBusinessActivitiesInfo(currentActivitiesInfo);
    }


}
