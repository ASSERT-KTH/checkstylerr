package com.java110.web.components.ownerRepair;

import com.java110.core.context.IPageData;
import com.java110.web.smo.ownerRepair.IEditOwnerRepairSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 编辑小区组件
 */
@Component("editOwnerRepair")
public class EditOwnerRepairComponent {

    @Autowired
    private IEditOwnerRepairSMO editOwnerRepairSMOImpl;

    /**
     * 添加小区数据
     *
     * @param pd 页面数据封装
     * @return ResponseEntity 对象
     */
    public ResponseEntity<String> update(IPageData pd) {
        return editOwnerRepairSMOImpl.updateOwnerRepair(pd);
    }

    public IEditOwnerRepairSMO getEditOwnerRepairSMOImpl() {
        return editOwnerRepairSMOImpl;
    }

    public void setEditOwnerRepairSMOImpl(IEditOwnerRepairSMO editOwnerRepairSMOImpl) {
        this.editOwnerRepairSMOImpl = editOwnerRepairSMOImpl;
    }
}
