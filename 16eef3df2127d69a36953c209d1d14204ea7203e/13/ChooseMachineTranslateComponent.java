package com.java110.web.components.machineTranslate;


import com.java110.core.context.IPageData;
import com.java110.web.smo.machineTranslate.IListMachineTranslatesSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * 应用组件管理类
 * <p>
 * add by wuxw
 * <p>
 * 2019-06-29
 */
@Component("chooseMachineTranslate")
public class ChooseMachineTranslateComponent {

    @Autowired
    private IListMachineTranslatesSMO listMachineTranslatesSMOImpl;

    /**
     * 查询应用列表
     *
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> list(IPageData pd) {
        return listMachineTranslatesSMOImpl.listMachineTranslates(pd);
    }

    public IListMachineTranslatesSMO getListMachineTranslatesSMOImpl() {
        return listMachineTranslatesSMOImpl;
    }

    public void setListMachineTranslatesSMOImpl(IListMachineTranslatesSMO listMachineTranslatesSMOImpl) {
        this.listMachineTranslatesSMOImpl = listMachineTranslatesSMOImpl;
    }
}
