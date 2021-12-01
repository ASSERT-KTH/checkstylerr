package com.java110.web.smo.basePrivilege.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.PrivilegeCodeConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.core.context.IPageData;
import com.java110.core.component.AbstractComponentSMO;
import com.java110.web.smo.basePrivilege.IEditBasePrivilegeSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 添加权限服务实现类
 * add by wuxw 2019-06-30
 */
@Service("eidtBasePrivilegeSMOImpl")
public class EditBasePrivilegeSMOImpl extends AbstractComponentSMO implements IEditBasePrivilegeSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {

        //super.validatePageInfo(pd);

        Assert.hasKeyAndValue(paramIn, "pId", "权限ID不能为空");
        Assert.hasKeyAndValue(paramIn, "name", "必填，请填写权限名称");
        Assert.hasKeyAndValue(paramIn, "domain", "必填，请选择商户类型");
        Assert.hasKeyAndValue(paramIn, "resource", "必填，请填写资源路径");


        super.checkUserHasPrivilege(pd, restTemplate, PrivilegeCodeConstant.BASE_PRIVILEGE);

    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ResponseEntity<String> responseEntity = null;
        super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/basePrivilege.updateBasePrivilege",
                HttpMethod.POST);
        return responseEntity;
    }

    @Override
    public ResponseEntity<String> updateBasePrivilege(IPageData pd) {
        return super.businessProcess(pd);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
