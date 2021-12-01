/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.job.adapt.ximoIot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.machine.MachineDto;
import com.java110.entity.order.Business;
import com.java110.intf.common.IMachineInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.ximoIot.asyn.IXimoMachineAsyn;
import com.java110.po.owner.OwnerPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

/**
 * HC iot 添加业主同步细末
 *
 * @desc add by 吴学文 18:58
 */
@Component(value = "ximoDeleteOwnerTransactionIotAdapt")
public class XimoDeleteOwnerTransactionIotAdapt extends DatabusAdaptImpl {

    @Autowired
    private IXimoMachineAsyn ximoMachineAsynImpl;
    @Autowired
    IMachineInnerServiceSMO machineInnerServiceSMOImpl;


    /**
     * accessToken={access_token}
     * &extCommunityUuid=01000
     * &extCommunityId=1
     * &devSn=111111111
     * &name=设备名称
     * &positionType=0
     * &positionUuid=1
     *
     * @param business   当前处理业务
     * @param businesses 所有业务信息
     */
    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        if (data.containsKey(OwnerPo.class.getSimpleName())) {
            Object bObj = data.get(OwnerPo.class.getSimpleName());
            JSONArray businessMachines = null;
            if (bObj instanceof JSONObject) {
                businessMachines = new JSONArray();
                businessMachines.add(bObj);
            } else if (bObj instanceof List) {
                businessMachines = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessMachines = (JSONArray) bObj;
            }
            for (int bOwnerIndex = 0; bOwnerIndex < businessMachines.size(); bOwnerIndex++) {
                JSONObject businessOwner = businessMachines.getJSONObject(bOwnerIndex);
                doSendMachine(business, businessOwner);
            }
        }
    }

    private void doSendMachine(Business business, JSONObject businessOwner) {

        OwnerPo ownerPo = BeanConvertUtil.covertBean(businessOwner, OwnerPo.class);

        //拿到小区ID
        String communityId = ownerPo.getCommunityId();
        //根据小区ID查询现有设备
        MachineDto machineDto = new MachineDto();
        machineDto.setCommunityId(communityId);
        //String[] locationObjIds = new String[]{communityId};
        List<String> locationObjIds = new ArrayList<>();
        locationObjIds.add(communityId);
        machineDto.setLocationObjIds(locationObjIds.toArray(new String[locationObjIds.size()]));
        List<MachineDto> machineDtos = machineInnerServiceSMOImpl.queryMachines(machineDto);
        Assert.listOnlyOne(machineDtos, "未找到设备");
        for (MachineDto tmpMachineDto : machineDtos) {
            if (!"9999".equals(tmpMachineDto.getMachineTypeCd())) {
                continue;
            }
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();

            postParameters.add("extCommunityUuid", ownerPo.getCommunityId());
            postParameters.add("uuids", ownerPo.getMemberId());
            ximoMachineAsynImpl.sendDeleteOwner(postParameters);
        }

    }
}
