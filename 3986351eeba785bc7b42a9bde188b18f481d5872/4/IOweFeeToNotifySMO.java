package com.java110.front.smo.payment;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * 房屋租赁微信通知 支付完成
 */
public interface IOweFeeToNotifySMO {

    /**
     * 支付完成
     * @param request
     * @return
     */
    public ResponseEntity<String> toNotify(String param,HttpServletRequest request);
}
