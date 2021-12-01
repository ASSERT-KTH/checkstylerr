package com.java110.web.smo.inspectionPoint;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加巡检点接口
 *
 * add by wuxw 2019-06-30
 */
public interface IAddInspectionPointSMO {

    /**
     * 添加巡检点
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> saveInspectionPoint(IPageData pd);
}
