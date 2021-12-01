package com.java110.web.smo.configMenu;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 添加配置菜单接口
 *
 * add by wuxw 2019-06-30
 */
public interface IConfigMenuBindingSMO {

    /**
     * 添加配置菜单
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    ResponseEntity<String> bindingConfigMenu(IPageData pd);
}
