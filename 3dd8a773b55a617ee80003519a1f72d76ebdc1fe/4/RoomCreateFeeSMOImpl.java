package com.java110.web.smo.feeConfig.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.component.AbstractComponentSMO;
import com.java110.core.context.IPageData;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.exception.SMOException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.web.smo.feeConfig.IListFeeConfigsSMO;
import com.java110.web.smo.feeConfig.IRoomCreateFeeSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 查询feeConfig服务类
 */
@Service("roomCreateFeeSMOImpl")
public class RoomCreateFeeSMOImpl extends AbstractComponentSMO implements IRoomCreateFeeSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> createFee(IPageData pd) throws SMOException {
        return businessProcess(pd);
    }

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {
        Assert.hasKeyAndValue(paramIn, "communityId", "未包含小区ID");
        Assert.hasKeyAndValue(paramIn, "locationTypeCd", "未包含收费范围");
        Assert.hasKeyAndValue(paramIn, "locationObjId", "未包含收费对象");
        Assert.hasKeyAndValue(paramIn, "configId", "未包含收费项目");
        Assert.hasKeyAndValue(paramIn, "billType", "未包含出账类型");
    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ComponentValidateResult result = super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        Map paramMap = BeanConvertUtil.beanCovertMap(result);
        paramIn.putAll(paramMap);

        String apiUrl = ServiceConstant.SERVICE_API_URL + "/api/fee.saveRoomCreateFee";


        ResponseEntity<String> responseEntity = this.callCenterService(restTemplate, pd, JSONObject.toJSONString(paramMap),
                apiUrl,
                HttpMethod.POST);

        return responseEntity;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
