package com.java110.web.components.machineTranslate;

import com.java110.core.context.IPageData;
import com.java110.web.smo.machineTranslate.IEditMachineTranslateSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 编辑小区组件
 */
@Component("editMachineTranslate")
public class EditMachineTranslateComponent {

    @Autowired
    private IEditMachineTranslateSMO editMachineTranslateSMOImpl;

    /**
     * 添加小区数据
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    public ResponseEntity<String> update(IPageData pd){
        return editMachineTranslateSMOImpl.updateMachineTranslate(pd);
    }

    public IEditMachineTranslateSMO getEditMachineTranslateSMOImpl() {
        return editMachineTranslateSMOImpl;
    }

    public void setEditMachineTranslateSMOImpl(IEditMachineTranslateSMO editMachineTranslateSMOImpl) {
        this.editMachineTranslateSMOImpl = editMachineTranslateSMOImpl;
    }
}
