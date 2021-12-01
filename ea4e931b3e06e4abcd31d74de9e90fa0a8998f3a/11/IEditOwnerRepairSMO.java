package com.java110.web.smo.ownerRepair;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 修改业主报修接口
 *
 * add by wuxw 2019-06-30
 */
public interface IEditOwnerRepairSMO {

    /**
     * 修改小区
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> updateOwnerRepair(IPageData pd);
}
