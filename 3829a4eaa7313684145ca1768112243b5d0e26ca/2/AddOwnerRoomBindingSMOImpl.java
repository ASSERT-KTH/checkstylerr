package com.java110.web.smo.addOwner.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.java110.utils.constant.PrivilegeCodeConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.web.smo.addOwner.IAddOwnerRoomBindingSMO;
import org.springframework.web.client.RestTemplate;
import com.java110.core.context.IPageData;
import com.java110.web.core.AbstractComponentSMO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 添加小区服务实现类
 * add by wuxw 2019-06-30
 */
@Service("addOwnerRoomBindingSMOImpl")
public class AddOwnerRoomBindingSMOImpl extends AbstractComponentSMO implements IAddOwnerRoomBindingSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {

        //super.validatePageInfo(pd);
        JSONArray infos = paramIn.getJSONArray("data");
        Assert.hasKeyAndValue(paramIn, "communityId", "未包含小区信息");

        if (infos.size() != 3) {
            throw new IllegalArgumentException("数据被篡改");
        }

        Assert.hasKeyByFlowData(infos, "viewFloorInfo", "floorId", "必填，未选择楼栋");
        Assert.hasKeyByFlowData(infos, "sellRoomSelectRoom", "roomId", "必填，未选择房屋");
        Assert.hasKeyByFlowData(infos, "viewOwnerInfo", "ownerId", "必填，未包含业主信息");


        super.checkUserHasPrivilege(pd, restTemplate, PrivilegeCodeConstant.ADD_OWNER_ROOM);

    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ResponseEntity<String> responseEntity = null;
        ComponentValidateResult result = super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        JSONArray infos = paramIn.getJSONArray("data");
        //JSONObject viewFloorInfo = getObj(infos, "viewFloorInfo");
        JSONObject sellRoomSelectRoom = getObj(infos, "sellRoomSelectRoom");
        JSONObject viewOwnerInfo = getObj(infos, "viewOwnerInfo");
        JSONObject newParamIn = new JSONObject();
        String communityId = paramIn.getString("communityId");
        newParamIn.put("ownerId", viewOwnerInfo.getString("ownerId"));
        newParamIn.put("roomId", sellRoomSelectRoom.getString("roomId"));
        newParamIn.put("communityId", communityId);
        newParamIn.put("userId", pd.getUserId());
        newParamIn.put("storeId", result.getStoreId());
        newParamIn.put("state", "2001");
        //房屋修改信息为已售
//        newParamIn.put("bId", sellRoomSelectRoom.getString("bId"));
//        newParamIn.put("roomNum", sellRoomSelectRoom.getString("roomNum"));
//        newParamIn.put("unitPrice", sellRoomSelectRoom.getString("unitPrice"));
//        newParamIn.put("builtUpArea", sellRoomSelectRoom.getString("builtUpArea"));
//        newParamIn.put("roomNum", sellRoomSelectRoom.getString("roomNum"));
//        newParamIn.put("unitNum", sellRoomSelectRoom.getString("unitNum"));
//        newParamIn.put("section", sellRoomSelectRoom.getString("section"));
//        newParamIn.put("flowComponent", sellRoomSelectRoom.getString("flowComponent"));
//        newParamIn.put("userName", sellRoomSelectRoom.getString("userName"));
//        newParamIn.put("roomId", sellRoomSelectRoom.getString("roomId"));
//        newParamIn.put("layer", sellRoomSelectRoom.getString("layer"));
//        newParamIn.put("apartment", sellRoomSelectRoom.getString("apartment"));

        responseEntity = this.callCenterService(restTemplate, pd, newParamIn.toJSONString(),
                ServiceConstant.SERVICE_API_URL + "/api/room.sellRoom",
                HttpMethod.POST);
        return responseEntity;
    }


    private JSONObject getObj(JSONArray infos, String flowComponent) {

        JSONObject serviceInfo = null;

        for (int infoIndex = 0; infoIndex < infos.size(); infoIndex++) {

            Assert.hasKeyAndValue(infos.getJSONObject(infoIndex), "flowComponent", "未包含服务流程组件名称");

            if (flowComponent.equals(infos.getJSONObject(infoIndex).getString("flowComponent"))) {
                serviceInfo = infos.getJSONObject(infoIndex);
                Assert.notNull(serviceInfo, "未包含服务信息");
                return serviceInfo;
            }
        }

        throw new IllegalArgumentException("未找到组件编码为【" + flowComponent + "】数据");
    }


    @Override
    public ResponseEntity<String> bindingAddOwnerRoom(IPageData pd) {
        return super.businessProcess(pd);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
