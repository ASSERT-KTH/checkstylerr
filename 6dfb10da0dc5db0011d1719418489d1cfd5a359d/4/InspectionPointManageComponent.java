package com.java110.web.components.inspectionPoint;


import com.java110.core.context.IPageData;
import com.java110.web.smo.inspectionPoint.IListInspectionPointsSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * 巡检点组件管理类
 *
 * add by wuxw
 *
 * 2019-06-29
 */
@Component("inspectionPointManage")
public class InspectionPointManageComponent {

    @Autowired
    private IListInspectionPointsSMO listInspectionPointsSMOImpl;

    /**
     * 查询巡检点列表
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> list(IPageData pd){
        return listInspectionPointsSMOImpl.listInspectionPoints(pd);
    }

    public IListInspectionPointsSMO getListInspectionPointsSMOImpl() {
        return listInspectionPointsSMOImpl;
    }

    public void setListInspectionPointsSMOImpl(IListInspectionPointsSMO listInspectionPointsSMOImpl) {
        this.listInspectionPointsSMOImpl = listInspectionPointsSMOImpl;
    }
}
