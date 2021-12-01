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
package com.java110.job.adapt.hcIot.asyn;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * IOT信息异步同步处理接口类
 * <p>
 * add by wuxw 2020-12-19
 * <p>
 * 接口协议地址： https://gitee.com/java110/MicroCommunityThings/blob/master/back/docs/api.md
 */
public interface IIotSendAsyn {

    /**
     * 添加小区信息
     *
     * @param postParameters
     */
    public void addCommunity(JSONObject postParameters);

    /**
     * 编辑小区信息
     *
     * @param postParameters
     */
    public void editCommunity(JSONObject postParameters);

    /**
     * 删除小区信息
     *
     * @param postParameters
     */
    public void deleteCommunity(JSONObject postParameters);

    /**
     * 添加 设备 至 HC IOT
     *
     * @param postParameters
     * @param ownerDtos
     */
    public void addMachine(JSONObject postParameters, List<JSONObject> ownerDtos);

    /**
     * 修改 设备 至 HC IOT
     *
     * @param postParameters
     */
    void updateMachine(JSONObject postParameters);

    void deleteMachine(JSONObject postParameters);

    public void addOwner(JSONObject postParameters);

    /**
     * 修改业主
     *
     * @param postParameters
     */
    void sendUpdateOwner(JSONObject postParameters);

    /**
     * 删除业主
     *
     * @param postParameters
     */
    void sendDeleteOwner(JSONObject postParameters);

    /**
     * 重新同步
     *
     * @param reqJson
     */
    void reSendIot(JSONObject reqJson);

    /**
     * 添加 停车场
     *
     * @param postParameters
     */
    void addParkingArea(JSONObject postParameters);

    void updateParkingArea(JSONObject postParameters);

    void deleteParkingArea(JSONObject postParameters);

    /**
     * 添加 车辆
     *
     * @param postParameters
     */
    void addOwnerCar(JSONObject postParameters);

    void updateOwnerCar(JSONObject postParameters);

    void deleteOwnerCar(JSONObject postParameters);

    void addCarBlackWhite(JSONObject postParameters);

    void deleteCarBlackWhite(JSONObject postParameters);
}
