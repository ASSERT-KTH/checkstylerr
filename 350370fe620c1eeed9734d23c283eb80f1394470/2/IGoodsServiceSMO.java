package com.java110.goods.smo;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.context.BusinessServiceDataFlow;
import com.java110.utils.exception.SMOException;

/**
 *
 * 用户信息管理，服务
 * Created by wuxw on 2017/4/5.
 */
public interface IGoodsServiceSMO {


    public JSONObject service(BusinessServiceDataFlow businessServiceDataFlow) throws SMOException;

}
