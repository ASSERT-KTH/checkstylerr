package com.java110.web.smo.inspectionRoute;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加巡检路线接口
 *
 * add by wuxw 2019-06-30
 */
public interface IAddInspectionRouteSMO {

    /**
     * 添加巡检路线
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> saveInspectionRoute(IPageData pd);
}
