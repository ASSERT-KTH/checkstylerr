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
package com.java110.job.adapt.hcIot.car;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.entity.order.Business;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.hcIot.asyn.IIotSendAsyn;
import com.java110.po.car.OwnerCarPo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * HC iot 车辆同步适配器
 * <p>
 * 接口协议地址： https://gitee.com/java110/MicroCommunityThings/blob/master/back/docs/api.md
 *
 * @desc add by 吴学文 18:58
 */
@Component(value = "addCarToIotAdapt")
public class AddCarToIotAdapt extends DatabusAdaptImpl {

    @Autowired
    private IIotSendAsyn hcOwnerCarAsynImpl;


    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;


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
        if (data.containsKey(OwnerCarPo.class.getSimpleName())) {
            Object bObj = data.get(OwnerCarPo.class.getSimpleName());
            JSONArray businessOwnerCars = null;
            if (bObj instanceof JSONObject) {
                businessOwnerCars = new JSONArray();
                businessOwnerCars.add(bObj);
            } else if (bObj instanceof List) {
                businessOwnerCars = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessOwnerCars = (JSONArray) bObj;
            }
            //JSONObject businessOwnerCar = data.getJSONObject("businessOwnerCar");
            for (int bOwnerCarIndex = 0; bOwnerCarIndex < businessOwnerCars.size(); bOwnerCarIndex++) {
                JSONObject businessOwnerCar = businessOwnerCars.getJSONObject(bOwnerCarIndex);
                doSendOwnerCar(business, businessOwnerCar);
            }
        }
    }

    private void doSendOwnerCar(Business business, JSONObject businessOwnerCar) {

        OwnerCarPo ownerCarPo = BeanConvertUtil.covertBean(businessOwnerCar, OwnerCarPo.class);

        OwnerCarDto ownerCarDto = new OwnerCarDto();
        ownerCarDto.setCarNum(ownerCarPo.getCarNum());
        ownerCarDto.setCommunityId(ownerCarPo.getCommunityId());
        List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);

        Assert.listOnlyOne(ownerCarDtos, "未找到车辆");

        //没有车位就不同步了
        if (StringUtil.isEmpty(ownerCarDtos.get(0).getPsId()) || "-1".equals(ownerCarDtos.get(0).getPsId())) {
            return;
        }

        //电动车
        //三轮车 不同步物联网系统
        if ("9904,9905".contains(ownerCarDtos.get(0).getCarType())) {
            return;
        }

        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setPsId(ownerCarDtos.get(0).getPsId());
        parkingSpaceDto.setCommunityId(ownerCarDtos.get(0).getCommunityId());
        List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
        Assert.listOnlyOne(ownerCarDtos, "未找到车位");


        JSONObject postParameters = new JSONObject();

        postParameters.put("carNum", ownerCarDtos.get(0).getCarNum());
        postParameters.put("startTime", DateUtil.getFormatTimeString(ownerCarDtos.get(0).getStartTime(), DateUtil.DATE_FORMATE_STRING_A));
        postParameters.put("endTime", DateUtil.getFormatTimeString(ownerCarDtos.get(0).getEndTime(), DateUtil.DATE_FORMATE_STRING_A));
        postParameters.put("extPaId", parkingSpaceDtos.get(0).getPaId());
        postParameters.put("personName", ownerCarDtos.get(0).getOwnerName());
        postParameters.put("personTel", ownerCarDtos.get(0).getLink());
        postParameters.put("extCarId", ownerCarDtos.get(0).getCarId());
        postParameters.put("extCommunityId", ownerCarDtos.get(0).getCommunityId());
        hcOwnerCarAsynImpl.addOwnerCar(postParameters);
    }
}
