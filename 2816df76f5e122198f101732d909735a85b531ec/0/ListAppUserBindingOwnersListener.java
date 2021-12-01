package com.java110.api.listener.owner;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.owner.IOwnerAppUserInnerServiceSMO;
import com.java110.core.smo.owner.IOwnerInnerServiceSMO;
import com.java110.core.smo.user.IUserInnerServiceSMO;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.user.UserDto;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.api.auditAppUserBindingOwner.ApiAuditAppUserBindingOwnerDataVo;
import com.java110.vo.api.auditAppUserBindingOwner.ApiAuditAppUserBindingOwnerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 查询小区侦听类
 */
@Java110Listener("listAppUserBindingOwnersListener")
public class ListAppUserBindingOwnersListener extends AbstractServiceApiListener {

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.LIST_APPUSERBINDINGOWNERS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //super.validatePageInfo(reqJson);

        Map<String, String> headers = event.getDataFlowContext().getRequestHeaders();

        Assert.hasKeyAndValue(headers, "userId", "请求头中未包含用户信息");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        Map<String, String> headers = event.getDataFlowContext().getRequestHeaders();

        String userId = headers.get("userId");

        //根据userId 查询openId
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(userDtos, "未找到相应用户信息，或查询到多条");

        String openId = userDtos.get(0).getOpenId();

        if(!reqJson.containsKey("page")){
            reqJson.put("page",1);
        }
        if(!reqJson.containsKey("row")){
            reqJson.put("row",10);
        }

        OwnerAppUserDto ownerAppUserDto = BeanConvertUtil.covertBean(reqJson, OwnerAppUserDto.class);
        ownerAppUserDto.setOpenId(openId);

        int count = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsersCount(ownerAppUserDto);

        List<ApiAuditAppUserBindingOwnerDataVo> auditAppUserBindingOwners = null;

        if (count > 0) {
            auditAppUserBindingOwners = BeanConvertUtil.covertBeanList(ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto), ApiAuditAppUserBindingOwnerDataVo.class);
        } else {
            auditAppUserBindingOwners = new ArrayList<>();
        }

        ApiAuditAppUserBindingOwnerVo apiAuditAppUserBindingOwnerVo = new ApiAuditAppUserBindingOwnerVo();

        apiAuditAppUserBindingOwnerVo.setTotal(count);
        apiAuditAppUserBindingOwnerVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiAuditAppUserBindingOwnerVo.setAuditAppUserBindingOwners(auditAppUserBindingOwners);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiAuditAppUserBindingOwnerVo), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }

    public IOwnerAppUserInnerServiceSMO getOwnerAppUserInnerServiceSMOImpl() {
        return ownerAppUserInnerServiceSMOImpl;
    }

    public void setOwnerAppUserInnerServiceSMOImpl(IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl) {
        this.ownerAppUserInnerServiceSMOImpl = ownerAppUserInnerServiceSMOImpl;
    }
}
