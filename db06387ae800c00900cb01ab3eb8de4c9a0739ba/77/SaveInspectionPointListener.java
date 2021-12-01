package com.java110.api.listener.inspectionPoint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.inspection.IInspectionBMO;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.utils.util.Assert;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.AppService;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ServiceCodeInspectionPointConstant;




import com.java110.core.annotation.Java110Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 保存巡检点
 * add by zc 2020-02-09
 */
@Java110Listener("saveInspectionPointListener")
public class SaveInspectionPointListener extends AbstractServiceApiListener {
    @Autowired
    private IInspectionBMO inspectionBMOImpl;
    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "machineId", "必填，请填写设备ID");
        Assert.hasKeyAndValue(reqJson, "inspectionName", "必填，请填写巡检点名称");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        HttpHeaders header = new HttpHeaders();
        context.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();

        AppService service = event.getAppService();

        //添加单元信息
        businesses.add(inspectionBMOImpl.addInspectionPoint(reqJson, context));

        ResponseEntity<String> responseEntity = inspectionBMOImpl.callService(context, service.getServiceCode(), businesses);

        context.setResponseEntity(responseEntity);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeInspectionPointConstant.ADD_INSPECTIONPOINT;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

}
