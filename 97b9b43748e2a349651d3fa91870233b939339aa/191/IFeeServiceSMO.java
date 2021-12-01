package com.java110.fee.smo;

import com.alibaba.fastjson.JSONObject;
import com.java110.common.exception.SMOException;
import com.java110.core.context.BusinessServiceDataFlow;

/**
 *
 * 用户信息管理，服务
 * Created by wuxw on 2017/4/5.
 */
public interface IFeeServiceSMO {


    public JSONObject service(BusinessServiceDataFlow businessServiceDataFlow) throws SMOException;

}
