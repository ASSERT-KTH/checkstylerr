package com.java110.core.base.smo;


import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.util.ProtocolUtil;
import com.java110.utils.util.StringUtil;
import com.java110.core.base.AppBase;
import com.java110.core.context.AppContext;
import com.java110.core.context.IPageData;
import com.java110.core.smo.code.IPrimaryKeyInnerServiceSMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 所有服务端的基类
 * 1、报文分装
 * 2、报文解析
 * Created by wuxw on 2017/2/28.
 */
public class BaseServiceSMO extends AppBase {

    private static final Logger logger = LoggerFactory.getLogger(BaseServiceSMO.class);

    /**
     * 主键生成
     *
     * @param iPrimaryKeyService 主键生成服务对象
     * @param type               主键类型 如 OL_ID , CUST_ID
     * @return
     * @throws Exception
     */
    protected String queryPrimaryKey(IPrimaryKeyInnerServiceSMO iPrimaryKeyService, String type) throws Exception {
        JSONObject data = new JSONObject();
        data.put("type", type);
        //生成的ID
        String targetId = "-1";
        //要求接口返回 {"RESULT_CODE":"0000","RESULT_INFO":{"user_id":"7020170411000041"},"RESULT_MSG":"成功"}
        String custIdJSONStr = iPrimaryKeyService.queryPrimaryKey(data.toJSONString());
        JSONObject custIdJSONTmp = JSONObject.parseObject(custIdJSONStr);
        if (custIdJSONTmp.containsKey("RESULT_CODE")
                && ProtocolUtil.RETURN_MSG_SUCCESS.equals(custIdJSONTmp.getString("RESULT_CODE"))
                && custIdJSONTmp.containsKey("RESULT_INFO")) {
            //从接口生成olId
            targetId = custIdJSONTmp.getJSONObject("RESULT_INFO").getString(type);
        }
        if ("-1".equals(targetId)) {
            throw new RuntimeException("调用主键生成服务服务失败，" + custIdJSONStr);
        }

        return targetId;
    }




    /**
     * 创建上下文对象
     *
     * @return
     */
    protected AppContext createApplicationContext() {
        return AppContext.newInstance();
    }


}
