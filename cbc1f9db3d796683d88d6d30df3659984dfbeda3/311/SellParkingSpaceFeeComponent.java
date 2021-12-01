package com.java110.web.components.parkingSpace;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.FeeTypeConstant;
import com.java110.core.context.IPageData;
import com.java110.web.smo.IFeeServiceSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @ClassName HireParkingSpaceFeeComponent
 * @Description TODO
 * @Author wuxw
 * @Date 2019/9/14 17:51
 * @Version 1.0
 * add by wuxw 2019/9/14
 **/
@Component("sellParkingSpaceFee")
public class SellParkingSpaceFeeComponent {

    @Autowired
    private IFeeServiceSMO feeServiceSMOImpl;


    /**
     * 查询出售费用配置
     *
     * @param pd 页面封装对象 包含页面请求数据
     * @return ResponseEntity对象返回给页面
     */
    public ResponseEntity<String> loadSellParkingSpaceConfigData(IPageData pd) {
        String paramIn = pd.getReqData();
        JSONObject paramObj = JSONObject.parseObject(paramIn);
        return feeServiceSMOImpl.loadPropertyConfigFee(pd, "1001".equals(paramObj.getString("typeCd"))
                ? FeeTypeConstant.FEE_TYPE_SELL_UP_PARKING_SPACE : FeeTypeConstant.FEE_TYPE_SELL_DOWN_PARKING_SPACE);
    }

    public IFeeServiceSMO getFeeServiceSMOImpl() {
        return feeServiceSMOImpl;
    }

    public void setFeeServiceSMOImpl(IFeeServiceSMO feeServiceSMOImpl) {
        this.feeServiceSMOImpl = feeServiceSMOImpl;
    }
}
