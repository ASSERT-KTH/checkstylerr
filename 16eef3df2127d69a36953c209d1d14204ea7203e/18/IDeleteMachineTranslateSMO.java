package com.java110.web.smo.machineTranslate;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加设备同步接口
 *
 * add by wuxw 2019-06-30
 */
public interface IDeleteMachineTranslateSMO {

    /**
     * 添加设备同步
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> deleteMachineTranslate(IPageData pd);
}
