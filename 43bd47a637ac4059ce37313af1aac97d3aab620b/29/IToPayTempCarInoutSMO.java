package com.java110.app.smo.payment;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 统一下单接口类
 */
public interface IToPayTempCarInoutSMO {

    /**
     * 下单
     * @param pd
     * @return
     */
    public ResponseEntity<String> toPay(IPageData pd);
}
