package com.java110.web.smo.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.StringUtil;
import com.java110.core.context.IPageData;
import com.java110.web.core.BaseComponentSMO;
import com.java110.web.smo.IStaffServiceSMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 员工服务类
 * Created by Administrator on 2019/4/2.
 */
@Service("staffServiceSMOImpl")
public class StaffServiceSMOImpl extends BaseComponentSMO implements IStaffServiceSMO {
    private final static Logger logger = LoggerFactory.getLogger(StaffServiceSMOImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 添加员工信息
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> saveStaff(IPageData pd) {
        logger.debug("保存员工信息入参：{}", pd.toString());
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        Assert.hasKeyAndValue(reqJson, "username", "请求报文格式错误或未包含用户名信息");
        Assert.hasKeyAndValue(reqJson, "email", "请求报文格式错误或未包含邮箱信息");
        Assert.hasKeyAndValue(reqJson, "tel", "请求报文格式错误或未包含手机信息");
        Assert.hasKeyAndValue(reqJson, "sex", "请求报文格式错误或未包含性别信息");
        Assert.hasKeyAndValue(reqJson, "address", "请求报文格式错误或未包含地址信息");
        Assert.hasKeyAndValue(reqJson, "orgId", "请求报文格式错误或未包含部门信息");
        Assert.hasKeyAndValue(reqJson, "relCd", "请求报文格式错误或未包含员工角色");


        ResponseEntity responseEntity = super.getStoreInfo(pd, restTemplate);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        Assert.jsonObjectHaveKey(responseEntity.getBody().toString(), "storeId", "根据用户ID查询商户ID失败，未包含storeId节点");

        String storeId = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeId");
        String storeTypeCd = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeTypeCd");
       // JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        reqJson.put("name", reqJson.getString("username"));
        reqJson.put("storeId", storeId);
        reqJson.put("storeTypeCd", storeTypeCd);
        responseEntity = this.callCenterService(restTemplate, pd, reqJson.toJSONString(), ServiceConstant.SERVICE_API_URL + "/api/user.staff.add", HttpMethod.POST);
        return responseEntity;
    }

    /**
     * 加载员工数据
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> loadData(IPageData pd) {


        Assert.jsonObjectHaveKey(pd.getReqData(), "page", "请求报文中未包含page节点");
        Assert.jsonObjectHaveKey(pd.getReqData(), "row", "请求报文中未包含rows节点");
        JSONObject paramIn = JSONObject.parseObject(pd.getReqData());
        Assert.isInteger(paramIn.getString("page"), "page不是数字");
        Assert.isInteger(paramIn.getString("row"), "rows不是数字");
        int page = Integer.parseInt(paramIn.getString("page"));
        int rows = Integer.parseInt(paramIn.getString("row"));
        String staffName = paramIn.getString("staffName");

        if (rows > 50) {
            return new ResponseEntity<String>("rows 数量不能大于50", HttpStatus.BAD_REQUEST);
        }
       // page = (page - 1) * rows;
        ResponseEntity responseEntity = super.getStoreInfo(pd, restTemplate);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        Assert.jsonObjectHaveKey(responseEntity.getBody().toString(), "storeId", "根据用户ID查询商户ID失败，未包含storeId节点");

        String storeId = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeId");
        //paramIn.put("page", page);
        paramIn.put("storeId", storeId);
        //if (StringUtil.isEmpty(staffName)) {
            responseEntity = this.callCenterService(restTemplate, pd, "",
                    ServiceConstant.SERVICE_API_URL + "/api/query.staff.infos" + super.mapToUrlParam(paramIn), HttpMethod.GET);
       /* } else {
            responseEntity = this.callCenterService(restTemplate, pd, "",
                    ServiceConstant.SERVICE_API_URL + "/api/query.staff.byName?rows=" + rows + "&page=" + page + "&storeId=" + storeId + "&name=" + staffName, HttpMethod.GET);
        }*/
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        JSONObject resultObjs = JSONObject.parseObject(responseEntity.getBody().toString());
        resultObjs.put("row", rows);
        resultObjs.put("page", page);
        return responseEntity;
    }

    /**
     * 修改员工信息
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> modifyStaff(IPageData pd) {

        ResponseEntity<String> responseEntity = null;
        //校验 前台数据
        modifyStaffValidate(pd);
        JSONObject paramIn = JSONObject.parseObject(pd.getReqData());
        paramIn.put("name", paramIn.getString("username"));
        //修改用户信息
        responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/user.staff.modify", HttpMethod.POST);
        return responseEntity;
    }

    /**
     * 删除工号
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> delete(IPageData pd) {
        ResponseEntity<String> responseEntity = null;
        Assert.jsonObjectHaveKey(pd.getReqData(), "userId", "请求报文格式错误或未包含用户ID信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "storeId", "请求报文格式错误或未包含商户ID信息");
        JSONObject paramIn = JSONObject.parseObject(pd.getReqData());
        JSONObject newParam = new JSONObject();
        newParam.put("userId", paramIn.getString("userId"));
        newParam.put("storeId", paramIn.getString("storeId"));
        //修改用户信息
        responseEntity = this.callCenterService(restTemplate, pd, newParam.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/user.staff.delete", HttpMethod.POST);
        return responseEntity;
    }

    /**
     * 查询 员工没有绑定的权限组
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> listNoAddPrivilegeGroup(IPageData pd) {

        ResponseEntity<String> responseEntity = null;
        Assert.jsonObjectHaveKey(pd.getReqData(), "userId", "请求报文格式错误或未包含用户ID信息");
        JSONObject _paramObj = JSONObject.parseObject(pd.getReqData());
        responseEntity = super.getStoreInfo(pd, restTemplate);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        Assert.jsonObjectHaveKey(responseEntity.getBody().toString(), "storeId", "根据用户ID查询商户ID失败，未包含storeId节点");

        String storeId = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeId");
        String storeTypeCd = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeTypeCd");
        //修改用户信息
        responseEntity = this.callCenterService(restTemplate, pd, "",
                ServiceConstant.SERVICE_API_URL + "/api/query.privilegeGroup.noAddPrivilegeGroup?userId="
                        + _paramObj.getString("userId") + "&storeId=" + storeId + "&storeTypeCd=" + storeTypeCd,
                HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        JSONObject outDataObj = JSONObject.parseObject(responseEntity.getBody());
        return new ResponseEntity<String>(outDataObj.getJSONArray("privilgeGroups").toJSONString(), HttpStatus.OK);
    }

    /**
     * 查询 员工没有绑定的权限
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> listNoAddPrivilege(IPageData pd) {
        ResponseEntity<String> responseEntity = null;
        Assert.jsonObjectHaveKey(pd.getReqData(), "userId", "请求报文格式错误或未包含用户ID信息");
        JSONObject _paramObj = JSONObject.parseObject(pd.getReqData());
        responseEntity = super.getStoreInfo(pd, restTemplate);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        Assert.jsonObjectHaveKey(responseEntity.getBody().toString(), "storeId", "根据用户ID查询商户ID失败，未包含storeId节点");

        String storeId = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeId");
        String storeTypeCd = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeTypeCd");
        //修改用户信息
        responseEntity = this.callCenterService(restTemplate, pd, "",
                ServiceConstant.SERVICE_API_URL + "/api/query.privilege.noAddPrivilege?userId="
                        + _paramObj.getString("userId") + "&storeId=" + storeId + "&storeTypeCd=" + storeTypeCd,
                HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        JSONObject outDataObj = JSONObject.parseObject(responseEntity.getBody());
        return new ResponseEntity<String>(outDataObj.getJSONArray("privilges").toJSONString(), HttpStatus.OK);
    }

    /**
     * 添加权限 或权限组
     *
     * @param pd
     * @return
     */
    @Override
    public ResponseEntity<String> addStaffPrivilegeOrPrivilegeGroup(IPageData pd) {
        ResponseEntity<String> responseEntity = null;
        Assert.jsonObjectHaveKey(pd.getReqData(), "userId", "请求报文格式错误或未包含用户ID信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "pId", "请求报文格式错误或未包含权限ID信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "pFlag", "请求报文格式错误");
        JSONObject _paramObj = JSONObject.parseObject(pd.getReqData());
        responseEntity = super.getStoreInfo(pd, restTemplate);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        Assert.jsonObjectHaveKey(responseEntity.getBody().toString(), "storeId", "根据用户ID查询商户ID失败，未包含storeId节点");

        String storeId = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeId");
        String storeTypeCd = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeTypeCd");
        _paramObj.put("storeId", storeId);
        _paramObj.put("storeTypeCd", storeTypeCd);

        //修改用户信息
        responseEntity = this.callCenterService(restTemplate, pd, _paramObj.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/add.privilege.userPrivilege",
                HttpMethod.POST);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> deleteStaffPrivilege(IPageData pd) {
        ResponseEntity<String> responseEntity = null;
        Assert.jsonObjectHaveKey(pd.getReqData(), "userId", "请求报文格式错误或未包含用户ID信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "pId", "请求报文格式错误或未包含权限ID信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "pFlag", "请求报文格式错误");
        JSONObject _paramObj = JSONObject.parseObject(pd.getReqData());
        responseEntity = super.getStoreInfo(pd, restTemplate);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        Assert.jsonObjectHaveKey(responseEntity.getBody().toString(), "storeId", "根据用户ID查询商户ID失败，未包含storeId节点");

        String storeId = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeId");
        String storeTypeCd = JSONObject.parseObject(responseEntity.getBody().toString()).getString("storeTypeCd");
        _paramObj.put("storeId", storeId);
        _paramObj.put("storeTypeCd", storeTypeCd);

        //修改用户信息
        responseEntity = this.callCenterService(restTemplate, pd, _paramObj.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/delete.privilege.userPrivilege",
                HttpMethod.POST);

        return responseEntity;
    }

    /**
     * 修改员工 数据校验
     *
     * @param pd
     */
    private void modifyStaffValidate(IPageData pd) {
        Assert.jsonObjectHaveKey(pd.getReqData(), "userId", "请求报文格式错误或未包含用户ID信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "username", "请求报文格式错误或未包含用户名信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "email", "请求报文格式错误或未包含邮箱信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "tel", "请求报文格式错误或未包含手机信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "sex", "请求报文格式错误或未包含性别信息");
        Assert.jsonObjectHaveKey(pd.getReqData(), "address", "请求报文格式错误或未包含地址信息");
    }


    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
