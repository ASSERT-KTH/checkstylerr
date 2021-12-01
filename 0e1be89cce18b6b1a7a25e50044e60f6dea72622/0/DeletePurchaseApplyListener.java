package com.java110.api.listener.purchaseApply;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.purchaseApply.IPurchaseApplyBMO;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.purchaseApply.IPurchaseApplyInnerServiceSMO;
import com.java110.dto.purchaseApply.PurchaseApplyDto;
import com.java110.entity.center.AppService;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodePurchaseApplyConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 删除采购/出库订单
 * add by zcc 2020/04/01
 */
@Java110Listener("deletePurchaseApplyListener")
public class DeletePurchaseApplyListener extends AbstractServiceApiListener {

    @Autowired
    private IPurchaseApplyBMO purchaseApplyBMOImpl;

    @Autowired
    private IPurchaseApplyInnerServiceSMO purchaseApplyInnerServiceSMOImpl;
    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "applyOrderId", "订单号不能为空");
        PurchaseApplyDto purchaseApplyDto = BeanConvertUtil.covertBean(reqJson, PurchaseApplyDto.class);
        List<PurchaseApplyDto> purchaseApplyDtos = purchaseApplyInnerServiceSMOImpl.queryPurchaseApplys(purchaseApplyDto);
        if(!"1000".equals(purchaseApplyDtos.get(0).getState())){
            throw new IllegalArgumentException("只能取消未审核的订单");
        }
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        HttpHeaders header = new HttpHeaders();
        context.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        AppService service = event.getAppService();
        businesses.add(deletePurchaseApply(reqJson, context));
        businesses.add(deletePurchaseApplyDetail(reqJson, context));
        ResponseEntity<String> responseEntity = purchaseApplyBMOImpl.callService(context, service.getServiceCode(), businesses);
        context.setResponseEntity(responseEntity);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodePurchaseApplyConstant.DELETE_PURCHASE_APPLY;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    /**
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
    private JSONObject deletePurchaseApply(JSONObject paramInJson, DataFlowContext dataFlowContext) {
        JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
        business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_DELETE_PURCHASE_APPLY);
        business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ);
        business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
        JSONObject businessPurchaseApply = new JSONObject();
        businessPurchaseApply.putAll(paramInJson);
        //计算 应收金额
        business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put("businessPurchaseApply", businessPurchaseApply);
        return business;
    }

    //删除订单明细
    private JSONObject deletePurchaseApplyDetail(JSONObject paramInJson, DataFlowContext dataFlowContext) {
        JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
        business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_DELETE_PURCHASE_APPLY_DETAIL);
        business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ);
        business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
        JSONObject businessPurchaseApply = new JSONObject();
        businessPurchaseApply.putAll(paramInJson);
        business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put("businessPurchaseApplyDetail", businessPurchaseApply);
        return business;
    }

}
