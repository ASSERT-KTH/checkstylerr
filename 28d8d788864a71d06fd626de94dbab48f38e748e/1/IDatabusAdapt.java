/**
 * Copyright 2017-2020 吴学文 and java110 team.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.job.adapt;

import com.alibaba.fastjson.JSONObject;
import com.java110.entity.order.Business;
import com.java110.vo.ResultVo;

import java.util.List;

/**
 * databus 适配器
 * <p>
 * add by wuxw 2020-12-07
 */
public interface IDatabusAdapt {

    /**
     * 业务处理
     *
     * @param business   当前处理业务
     * @param businesses 所有业务信息
     */
    public void execute(Business business, List<Business> businesses);

    /**
     * 业务处理
     *
     * @param paramIn   业务信息
     */
    public ResultVo openDoor(JSONObject paramIn);

}
