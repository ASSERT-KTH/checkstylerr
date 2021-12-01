package com.java110.web.smo;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 小区楼接口类
 */
public interface IParkingSpaceServiceSMO {

    /**
     * 查询小区楼信息
     *
     * @param pd 页面数据封装对象
     * @return 返回 ResponseEntity对象包含 http状态 信息 body信息
     */
    ResponseEntity<String> listParkingSpace(IPageData pd);


    /**
     * 添加小区楼信息
     *
     * @param pd 页面数据封装对象
     * @return 返回 ResponseEntity对象包含 http状态 信息 body信息
     */
    ResponseEntity<String> saveParkingSpace(IPageData pd);

    /**
     * 编辑小区楼信息
     *
     * @param pd 页面数据封装对象
     * @return 返回 ResponseEntity对象包含 http状态 信息 body信息
     */
    ResponseEntity<String> editParkingSpace(IPageData pd);

    /**
     * 删除小区楼
     *
     * @param pd 页面数据封装对象
     * @return 返回 ResponseEntity对象包含 http状态 信息 body信息
     */
    ResponseEntity<String> deleteParkingSpace(IPageData pd);

    /**
     * 查询业主 停车位
     *
     * @param pd 页面数据封装
     * @return 返回停车位信息
     */
    ResponseEntity<String> listParkingSpaceByOwner(IPageData pd);

    /**
     * 退停车位
     * @param pd
     * @return
     */
    ResponseEntity<String> exitParkingSpace(IPageData pd);
}
