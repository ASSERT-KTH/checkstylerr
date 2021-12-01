package com.java110.web.smo.menuGroup.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.PrivilegeCodeConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import org.springframework.web.client.RestTemplate;
import com.java110.core.context.IPageData;
import com.java110.core.component.AbstractComponentSMO;
import com.java110.web.smo.menuGroup.IAddMenuGroupSMO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 添加小区服务实现类
 * add by wuxw 2019-06-30
 */
@Service("addMenuGroupSMOImpl")
public class AddMenuGroupSMOImpl extends AbstractComponentSMO implements IAddMenuGroupSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {

        //super.validatePageInfo(pd);

        //Assert.hasKeyAndValue(paramIn, "xxx", "xxx");
        Assert.hasKeyAndValue(paramIn, "name", "必填，请填写组名称");
        Assert.hasKeyAndValue(paramIn, "icon", "必填，请填写icon");
        Assert.hasKeyAndValue(paramIn, "label", "必填，请填写标签");
        Assert.hasKeyAndValue(paramIn, "seq", "必填，请填写序列");


        super.checkUserHasPrivilege(pd, restTemplate, PrivilegeCodeConstant.MENU);

    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ResponseEntity<String> responseEntity = null;
        super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/menuGroup.saveMenuGroup",
                HttpMethod.POST);
        return responseEntity;
    }

    @Override
    public ResponseEntity<String> saveMenuGroup(IPageData pd) {
        return super.businessProcess(pd);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
