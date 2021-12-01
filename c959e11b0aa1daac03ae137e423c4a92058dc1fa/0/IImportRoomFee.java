package com.java110.fee.bmo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;

/**
 * 导入房屋费用
 */
public interface IImportRoomFee {

    /**
     * 欠费缴费接口
     * @param reqJson 缴费报文
     * @return
     */
    ResponseEntity<String> importFee(JSONObject reqJson);
}
