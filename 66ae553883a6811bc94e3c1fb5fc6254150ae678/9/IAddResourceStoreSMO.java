package com.java110.web.smo.resourceStore;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加物品管理接口
 *
 * add by wuxw 2019-06-30
 */
public interface IAddResourceStoreSMO {

    /**
     * 添加物品管理
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> saveResourceStore(IPageData pd);
}
