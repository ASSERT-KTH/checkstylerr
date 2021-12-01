package com.java110.api.bmo.activities;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.IApiBaseBMO;
import com.java110.core.context.DataFlowContext;

public interface IActivitiesBMO extends IApiBaseBMO {

    /**
     * 添加物业费用
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject addHeaderImg(JSONObject paramInJson, DataFlowContext dataFlowContext);


    /**
     * 添加活动
     * @param paramInJson
     * @param dataFlowContext
     * @return
     */
     JSONObject addActivities(JSONObject paramInJson, DataFlowContext dataFlowContext);

    /**
     * 修改头部照片
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject editHeaderImg(JSONObject paramInJson, DataFlowContext dataFlowContext);

    /**
     * 添加活动信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject updateActivities(JSONObject paramInJson, DataFlowContext dataFlowContext);

    /**
     * 删除活动
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject deleteActivities(JSONObject paramInJson, DataFlowContext dataFlowContext);



}
