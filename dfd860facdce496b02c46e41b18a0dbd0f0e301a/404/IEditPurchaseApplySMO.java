package com.java110.front.smo.purchaseApply;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 修改采购申请接口
 *
 * add by wuxw 2019-06-30
 */
public interface IEditPurchaseApplySMO {

    /**
     * 修改小区
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> updatePurchaseApply(IPageData pd);
}
