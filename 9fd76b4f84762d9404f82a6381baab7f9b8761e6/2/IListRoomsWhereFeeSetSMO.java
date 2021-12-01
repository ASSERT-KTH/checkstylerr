package com.java110.web.smo.feeConfig;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * @ClassName IListRoomsWhereFeeSet
 * @Description TODO 查询将要设置费用的房屋
 * @Author wuxw
 * @Date 2020/1/30 21:47
 * @Version 1.0
 * add by wuxw 2020/1/30
 **/
public interface IListRoomsWhereFeeSetSMO {

    ResponseEntity<String> listRooms(IPageData pd);
}
