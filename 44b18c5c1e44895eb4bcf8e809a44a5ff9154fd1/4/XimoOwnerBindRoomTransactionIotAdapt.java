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
import com.java110.dto.RoomDto;
import com.java110.dto.file.FileDto;
import com.java110.dto.file.FileRelDto;
import com.java110.dto.machine.MachineDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.entity.order.Business;
import com.java110.intf.common.IFileInnerServiceSMO;
import com.java110.intf.common.IFileRelInnerServiceSMO;
import com.java110.intf.common.IMachineInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.ximoIot.asyn.IXimoMachineAsyn;
import com.java110.po.owner.OwnerPo;
import com.java110.po.owner.OwnerRoomRelPo;
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
@Component(value = "ximoOwnerBindRoomTransactionIotAdapt")
public class XimoOwnerBindRoomTransactionIotAdapt extends DatabusAdaptImpl {

    @Autowired
    private IXimoMachineAsyn ximoMachineAsynImpl;
    @Autowired
    IMachineInnerServiceSMO machineInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFileRelInnerServiceSMO fileRelInnerServiceSMOImpl;

    @Autowired
    private IFileInnerServiceSMO fileInnerServiceSMOImpl;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;

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

        OwnerRoomRelPo ownerRoomRelPo = BeanConvertUtil.covertBean(businessOwner, OwnerRoomRelPo.class);

        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setOwnerId(ownerRoomRelPo.getOwnerId());
        List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwners(ownerDto);
        for (OwnerDto tOwnerDto : ownerDtos) {
            dealOwnerData(tOwnerDto, ownerRoomRelPo);
        }


    }

    private void dealOwnerData(OwnerDto tOwnerDto, OwnerRoomRelPo ownerRoomRelPo) {

        FileRelDto fileRelDto = new FileRelDto();
        fileRelDto.setObjId(tOwnerDto.getMemberId());
        fileRelDto.setRelTypeCd("10000");
        List<FileRelDto> fileRelDtos = fileRelInnerServiceSMOImpl.queryFileRels(fileRelDto);
        if (fileRelDtos == null || fileRelDtos.size() != 1) {
            return;
        }
        FileDto fileDto = new FileDto();
        fileDto.setFileId(fileRelDtos.get(0).getFileSaveName());
        fileDto.setFileSaveName(fileRelDtos.get(0).getFileSaveName());
        fileDto.setCommunityId(tOwnerDto.getCommunityId());
        List<FileDto> fileDtos = fileInnerServiceSMOImpl.queryFiles(fileDto);
        if (fileDtos == null || fileDtos.size() != 1) {
            return;
        }

        RoomDto roomDto = new RoomDto();
        roomDto.setRoomId(ownerRoomRelPo.getRoomId());
        roomDto.setCommunityId(tOwnerDto.getCommunityId());
        //这种情况说明 业主已经删掉了 需要查询状态为 1 的数据
        List<RoomDto> rooms = roomInnerServiceSMOImpl.queryRoomsByOwner(roomDto);

        //拿到小区ID
        String communityId = tOwnerDto.getCommunityId();
        //根据小区ID查询现有设备
        MachineDto machineDto = new MachineDto();
        machineDto.setCommunityId(communityId);
        //String[] locationObjIds = new String[]{communityId};
        List<String> locationObjIds = new ArrayList<>();
        locationObjIds.add(communityId);
        for (RoomDto tRoomDto : rooms) {
            locationObjIds.add(tRoomDto.getUnitId());
            locationObjIds.add(tRoomDto.getRoomId());
        }

        machineDto.setLocationObjIds(locationObjIds.toArray(new String[locationObjIds.size()]));
        List<MachineDto> machineDtos = machineInnerServiceSMOImpl.queryMachines(machineDto);
        Assert.listOnlyOne(machineDtos, "未找到设备");
        for (MachineDto tmpMachineDto : machineDtos) {
            if (!"9999".equals(tmpMachineDto.getMachineTypeCd())) {
                continue;
            }
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();

            postParameters.add("extCommunityUuid", tOwnerDto.getCommunityId());
            postParameters.add("addAuthorizationDevSn", tmpMachineDto.getMachineCode());
            postParameters.add("uuid", tOwnerDto.getMemberId());
            postParameters.add("name", tOwnerDto.getName());
            postParameters.add("faceFileBase64Array", fileDtos.get(0).getContext());
            ximoMachineAsynImpl.sendOwner(postParameters);
        }
    }
}
