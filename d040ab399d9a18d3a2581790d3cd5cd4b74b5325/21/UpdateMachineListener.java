package com.java110.api.listener.machine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.machine.IMachineBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.hardwareAdapation.IMachineInnerServiceSMO;
import com.java110.entity.center.AppService;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeMachineConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 保存设备侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("updateMachineListener")
public class UpdateMachineListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IMachineInnerServiceSMO machineInnerServiceSMOImpl;

    @Autowired
    private IMachineBMO machineBMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "machineId", "设备ID不能为空");
        Assert.hasKeyAndValue(reqJson, "machineCode", "必填，请填写设备编码");
        Assert.hasKeyAndValue(reqJson, "machineVersion", "必填，请填写设备版本号");
        Assert.hasKeyAndValue(reqJson, "machineName", "必填，请填写设备名称");
        Assert.hasKeyAndValue(reqJson, "machineTypeCd", "必填，请选择设备类型");
        Assert.hasKeyAndValue(reqJson, "direction", "必填，请选择设备方向");
        Assert.hasKeyAndValue(reqJson, "authCode", "必填，请填写鉴权编码");
        Assert.hasKeyAndValue(reqJson, "locationTypeCd", "必填，请选择位置类型");
        Assert.hasKeyAndValue(reqJson, "locationObjId", "必填，请填写位置对象ID");

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {


        machineBMOImpl.updateMachine(reqJson, context);


    }

    @Override
    public String getServiceCode() {
        return ServiceCodeMachineConstant.UPDATE_MACHINE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IMachineInnerServiceSMO getMachineInnerServiceSMOImpl() {
        return machineInnerServiceSMOImpl;
    }

    public void setMachineInnerServiceSMOImpl(IMachineInnerServiceSMO machineInnerServiceSMOImpl) {
        this.machineInnerServiceSMOImpl = machineInnerServiceSMOImpl;
    }
}
