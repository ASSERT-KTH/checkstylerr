package com.java110.api.bmo.advert;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.IApiBaseBMO;
import com.java110.core.context.DataFlowContext;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.advert.AdvertDto;
import com.java110.dto.advert.AdvertItemDto;
import com.java110.dto.file.FileDto;
import com.java110.dto.file.FileRelDto;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.util.Assert;

import java.util.List;

/**
 * 广告相关业务类
 */
public interface IAdvertBMO extends IApiBaseBMO {

    /**
     * 添加小区信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject deleteAdvert(JSONObject paramInJson, DataFlowContext dataFlowContext);

     JSONObject addAdvertItemPhoto(JSONObject paramInJson, DataFlowContext dataFlowContext, String photo);

    /**
     * 添加小区信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject addAdvertItemVedio(JSONObject paramInJson, DataFlowContext dataFlowContext);

    /**
     * 添加物业费用
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject addAdvertFileRel(JSONObject paramInJson, DataFlowContext dataFlowContext, String relTypeCd);

    /**
     * 添加小区信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject addAdvert(JSONObject paramInJson, DataFlowContext dataFlowContext);


    /**
     * 删除所有的照片或视频信息
     *
     * @param advertItemDto
     * @param context
     * @return
     */
     JSONObject delAdvertItemPhotoOrVideo(AdvertItemDto advertItemDto, DataFlowContext context);


    /**
     * 删除广告文件关系
     *
     * @param fileRelDto      接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject delAdvertFileRel(FileRelDto fileRelDto, DataFlowContext dataFlowContext);

    /**
     * 添加发布广告信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     JSONObject updateAdvert(JSONObject paramInJson, DataFlowContext dataFlowContext) ;

}
