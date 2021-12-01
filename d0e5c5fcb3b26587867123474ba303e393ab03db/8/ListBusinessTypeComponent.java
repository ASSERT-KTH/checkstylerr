package com.java110.web.components.businessType;

import com.java110.core.context.IPageData;
import com.java110.web.smo.IBusinessTypeServiceSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("listBusinessType")
public class ListBusinessTypeComponent {
    @Autowired
    private IBusinessTypeServiceSMO businessTypeServiceSMOImpl;

    /**
     * 查询小区楼信息
     *
     * @param pd 页面封装对象 包含页面请求数据
     * @return ResponseEntity对象返回给页面
     */
    public ResponseEntity<String> list(IPageData pd) {

        return businessTypeServiceSMOImpl.listBusinessType(pd);
    }


}
