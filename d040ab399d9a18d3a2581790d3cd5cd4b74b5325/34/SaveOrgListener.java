package com.java110.api.listener.org;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.org.IOrgBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.entity.center.AppService;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeOrgConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 保存小区侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("saveOrgListener")
public class SaveOrgListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IOrgBMO orgBMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");

        Assert.hasKeyAndValue(reqJson, "orgName", "必填，请填写组织名称");
        Assert.hasKeyAndValue(reqJson, "orgLevel", "必填，请填写报修人名称");
        Assert.hasKeyAndValue(reqJson, "parentOrgId", "必填，请选择上级ID");
        //Assert.hasKeyAndValue(reqJson, "belongCommunityId", "必填，请选择隶属小区");
        //Assert.hasKeyAndValue(reqJson, "description", "必填，请填写描述");
        Assert.hasKeyAndValue(reqJson, "storeId", "必填，请填写商户ID");

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        orgBMOImpl.addOrg(reqJson, context);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeOrgConstant.ADD_ORG;
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
