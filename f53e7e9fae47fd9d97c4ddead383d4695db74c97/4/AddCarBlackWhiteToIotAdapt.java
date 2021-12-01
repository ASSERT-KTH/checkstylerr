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
import com.java110.dto.machine.CarBlackWhiteDto;
import com.java110.entity.order.Business;
import com.java110.intf.common.ICarBlackWhiteInnerServiceSMO;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.adapt.hcIot.asyn.IIotSendAsyn;
import com.java110.po.car.CarBlackWhitePo;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * HC iot 车辆黑白名单同步适配器
 * <p>
 * 接口协议地址： https://gitee.com/java110/MicroCommunityThings/blob/master/back/docs/api.md
 *
 * @desc add by 吴学文 18:58
 */
@Component(value = "addCarBlackWhiteToIotAdapt")
public class AddCarBlackWhiteToIotAdapt extends DatabusAdaptImpl {

    @Autowired
    private IIotSendAsyn hcCarBlackWhiteAsynImpl;


    @Autowired
    private ICarBlackWhiteInnerServiceSMO carBlackWhiteInnerServiceSMOImpl;

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
        if (data.containsKey(CarBlackWhitePo.class.getSimpleName())) {
            Object bObj = data.get(CarBlackWhitePo.class.getSimpleName());
            JSONArray businessCarBlackWhites = null;
            if (bObj instanceof JSONObject) {
                businessCarBlackWhites = new JSONArray();
                businessCarBlackWhites.add(bObj);
            } else if (bObj instanceof List) {
                businessCarBlackWhites = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessCarBlackWhites = (JSONArray) bObj;
            }
            //JSONObject businessCarBlackWhite = data.getJSONObject("businessCarBlackWhite");
            for (int bCarBlackWhiteIndex = 0; bCarBlackWhiteIndex < businessCarBlackWhites.size(); bCarBlackWhiteIndex++) {
                JSONObject businessCarBlackWhite = businessCarBlackWhites.getJSONObject(bCarBlackWhiteIndex);
                doSendCarBlackWhite(business, businessCarBlackWhite);
            }
        }
    }

    private void doSendCarBlackWhite(Business business, JSONObject businessCarBlackWhite) {

        CarBlackWhitePo carBlackWhitePo = BeanConvertUtil.covertBean(businessCarBlackWhite, CarBlackWhitePo.class);

        CarBlackWhiteDto carBlackWhiteDto = new CarBlackWhiteDto();
        carBlackWhiteDto.setCarNum(carBlackWhitePo.getCarNum());
        carBlackWhiteDto.setCommunityId(carBlackWhitePo.getCommunityId());
        List<CarBlackWhiteDto> carBlackWhiteDtos = carBlackWhiteInnerServiceSMOImpl.queryCarBlackWhites(carBlackWhiteDto);

        Assert.listOnlyOne(carBlackWhiteDtos, "未找到车辆黑白名单");


        JSONObject postParameters = new JSONObject();

        postParameters.put("carNum", carBlackWhiteDtos.get(0).getCarNum());
        postParameters.put("startTime", carBlackWhiteDtos.get(0).getStartTime());
        postParameters.put("endTime", carBlackWhiteDtos.get(0).getEndTime());
        postParameters.put("extPaId", carBlackWhiteDtos.get(0).getPaId());
        postParameters.put("blackWhite", carBlackWhiteDtos.get(0).getBlackWhite());
        postParameters.put("extBwId", carBlackWhiteDtos.get(0).getBwId());
        postParameters.put("extCommunityId", carBlackWhiteDtos.get(0).getCommunityId());
        hcCarBlackWhiteAsynImpl.addCarBlackWhite(postParameters);
    }
}
