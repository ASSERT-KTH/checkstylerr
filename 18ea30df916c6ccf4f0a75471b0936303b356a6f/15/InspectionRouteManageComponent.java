package com.java110.web.components.inspectionRoute;


import com.java110.core.context.IPageData;
import com.java110.web.smo.inspectionRoute.IListInspectionRoutesSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * 巡检路线组件管理类
 *
 * add by wuxw
 *
 * 2019-06-29
 */
@Component("inspectionRouteManage")
public class InspectionRouteManageComponent {

    @Autowired
    private IListInspectionRoutesSMO listInspectionRoutesSMOImpl;

    /**
     * 查询巡检路线列表
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> list(IPageData pd){
        return listInspectionRoutesSMOImpl.listInspectionRoutes(pd);
    }

    public IListInspectionRoutesSMO getListInspectionRoutesSMOImpl() {
        return listInspectionRoutesSMOImpl;
    }

    public void setListInspectionRoutesSMOImpl(IListInspectionRoutesSMO listInspectionRoutesSMOImpl) {
        this.listInspectionRoutesSMOImpl = listInspectionRoutesSMOImpl;
    }
}
