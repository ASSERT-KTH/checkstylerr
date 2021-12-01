package com.java110.web.components.machine;

import com.java110.core.context.IPageData;
import com.java110.web.smo.IFloorServiceSMO;
import com.java110.web.smo.machine.IListMachinesSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 查询设备组件
 */
@Component("machineSelect2")
public class MachineSelect2Component {

    @Autowired
    private IListMachinesSMO listMachinesSMOImpl;

    /**
     * 查询设备列表
     *
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> list(IPageData pd) {
        return listMachinesSMOImpl.listMachines(pd);
    }

    public IListMachinesSMO getListMachinesSMOImpl() {
        return listMachinesSMOImpl;
    }

    public void setListMachinesSMOImpl(IListMachinesSMO listMachinesSMOImpl) {
        this.listMachinesSMOImpl = listMachinesSMOImpl;
    }
}
