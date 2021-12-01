package com.java110.web.components.auditUser;


import com.java110.core.context.IPageData;
import com.java110.web.smo.auditUser.IListAuditUsersSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * 审核人员组件管理类
 * <p>
 * add by wuxw
 * <p>
 * 2019-06-29
 */
@Component("auditUserManage")
public class AuditUserManageComponent {

    @Autowired
    private IListAuditUsersSMO listAuditUsersSMOImpl;

    /**
     * 查询审核人员列表
     *
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> list(IPageData pd) {
        return listAuditUsersSMOImpl.listAuditUsers(pd);
    }

    public IListAuditUsersSMO getListAuditUsersSMOImpl() {
        return listAuditUsersSMOImpl;
    }

    public void setListAuditUsersSMOImpl(IListAuditUsersSMO listAuditUsersSMOImpl) {
        this.listAuditUsersSMOImpl = listAuditUsersSMOImpl;
    }
}
