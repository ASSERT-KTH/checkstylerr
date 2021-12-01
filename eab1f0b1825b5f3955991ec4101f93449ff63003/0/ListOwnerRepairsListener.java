package com.java110.api.listener.ownerRepair;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.repair.IRepairInnerServiceSMO;
import com.java110.dto.repair.RepairDto;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeOwnerRepairConstant;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.ownerRepair.ApiOwnerRepairDataVo;
import com.java110.vo.api.ownerRepair.ApiOwnerRepairVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 查询小区侦听类
 */
@Java110Listener("listOwnerRepairsListener")
public class ListOwnerRepairsListener extends AbstractServiceApiListener {

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeOwnerRepairConstant.LIST_OWNERREPAIRS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IRepairInnerServiceSMO getRepairInnerServiceSMOImpl() {
        return repairInnerServiceSMOImpl;
    }

    public void setRepairInnerServiceSMOImpl(IRepairInnerServiceSMO repairInnerServiceSMOImpl) {
        this.repairInnerServiceSMOImpl = repairInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        RepairDto ownerRepairDto = BeanConvertUtil.covertBean(reqJson, RepairDto.class);

        if(!StringUtil.isEmpty(ownerRepairDto.getRoomId()) && ownerRepairDto.getRoomId().contains(",")){
            String[] roomIds = ownerRepairDto.getRoomId().split(",");
            ownerRepairDto.setRoomIds(roomIds);
            ownerRepairDto.setRoomId("");
        }

        int count = repairInnerServiceSMOImpl.queryRepairsCount(ownerRepairDto);

        List<ApiOwnerRepairDataVo> ownerRepairs = null;

        if (count > 0) {
            ownerRepairs = BeanConvertUtil.covertBeanList(repairInnerServiceSMOImpl.queryRepairs(ownerRepairDto), ApiOwnerRepairDataVo.class);
        } else {
            ownerRepairs = new ArrayList<>();
        }

        ApiOwnerRepairVo apiOwnerRepairVo = new ApiOwnerRepairVo();

        apiOwnerRepairVo.setTotal(count);
        apiOwnerRepairVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiOwnerRepairVo.setOwnerRepairs(ownerRepairs);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiOwnerRepairVo), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }
}
