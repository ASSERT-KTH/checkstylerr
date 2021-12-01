package com.java110.web.components.parkingArea;

import com.java110.core.context.IPageData;
import com.java110.web.smo.parkingArea.IEditParkingAreaSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 编辑小区组件
 */
@Component("editParkingArea")
public class EditParkingAreaComponent {

    @Autowired
    private IEditParkingAreaSMO editParkingAreaSMOImpl;

    /**
     * 添加小区数据
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    public ResponseEntity<String> update(IPageData pd){
        return editParkingAreaSMOImpl.updateParkingArea(pd);
    }

    public IEditParkingAreaSMO getEditParkingAreaSMOImpl() {
        return editParkingAreaSMOImpl;
    }

    public void setEditParkingAreaSMOImpl(IEditParkingAreaSMO editParkingAreaSMOImpl) {
        this.editParkingAreaSMOImpl = editParkingAreaSMOImpl;
    }
}
