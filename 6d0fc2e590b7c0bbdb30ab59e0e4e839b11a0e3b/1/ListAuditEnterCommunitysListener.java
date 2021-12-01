package com.java110.api.listener.community;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.common.constant.ServiceCodeConstant;
import com.java110.common.util.BeanConvertUtil;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.community.ICommunityInnerServiceSMO;
import com.java110.core.smo.store.IStoreInnerServiceSMO;
import com.java110.dto.CommunityMemberDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.store.StoreDto;
import com.java110.event.service.api.ServiceDataFlowEvent;
import com.java110.vo.api.community.ApiCommunityDataVo;
import com.java110.vo.api.community.ApiCommunityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 查询需要入驻审核单子
 */
@Java110Listener("listAuditEnterCommunitysListener")
public class ListAuditEnterCommunitysListener extends AbstractServiceApiListener {

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMOImpl;

    @Autowired
    private IStoreInnerServiceSMO storeInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_LIST_AUDIT_ENTER_COMMUNITYS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public ICommunityInnerServiceSMO getCommunityInnerServiceSMOImpl() {
        return communityInnerServiceSMOImpl;
    }

    public void setCommunityInnerServiceSMOImpl(ICommunityInnerServiceSMO communityInnerServiceSMOImpl) {
        this.communityInnerServiceSMOImpl = communityInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        CommunityMemberDto communityMemberDto = BeanConvertUtil.covertBean(reqJson, CommunityMemberDto.class);

        communityMemberDto.setNeedCommunityInfo(true);

        int count = communityInnerServiceSMOImpl.getCommunityMemberCount(communityMemberDto);

        List<ApiCommunityDataVo> communitys = null;

        if (count > 0) {
            communitys = BeanConvertUtil.covertBeanList(communityInnerServiceSMOImpl.getCommunityMembers(communityMemberDto), ApiCommunityDataVo.class);
        } else {
            communitys = new ArrayList<>();
        }

        //刷入 商户信息
        if(communitys.size() > 0){
            freshCommunityInfo(communitys);
        }

        ApiCommunityVo apiCommunityVo = new ApiCommunityVo();

        apiCommunityVo.setTotal(count);
        apiCommunityVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiCommunityVo.setCommunitys(communitys);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiCommunityVo), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }

    /**
     * 刷新 小区 商户信息
     * @param communityDataVos
     */
    private void freshCommunityInfo(List<ApiCommunityDataVo> communityDataVos){
        for(ApiCommunityDataVo apiCommunityDataVo : communityDataVos) {
            StoreDto storeDto = new StoreDto();
            storeDto.setStoreId(apiCommunityDataVo.getMemberId());
            List<StoreDto> storeDtos = storeInnerServiceSMOImpl.getStores(storeDto);

            if(storeDtos.size() != 1){
                continue;
            }
            BeanConvertUtil.covertBean(storeDtos.get(0), apiCommunityDataVo);
        }
    }

    public IStoreInnerServiceSMO getStoreInnerServiceSMOImpl() {
        return storeInnerServiceSMOImpl;
    }

    public void setStoreInnerServiceSMOImpl(IStoreInnerServiceSMO storeInnerServiceSMOImpl) {
        this.storeInnerServiceSMOImpl = storeInnerServiceSMOImpl;
    }
}
