package com.java110.web.components.community;


import com.alibaba.fastjson.JSONObject;
import com.java110.core.context.IPageData;
import com.java110.core.context.PageData;
import com.java110.web.smo.community.IAuditEnterCommunitySMO;
import com.java110.web.smo.community.IListAuditEnterCommunitysSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * 小区审核
 * <p>
 * add by wuxw
 * <p>
 * 2019-06-29
 */
@Component("recallAuditEnterFinishCommunity")
public class RecallAuditEnterFinishCommunityComponent {

    @Autowired
    private IAuditEnterCommunitySMO auditEnterCommunitySMOImpl;


    /**
     * 审核 小区
     *
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> recall(IPageData pd) {
        return auditEnterCommunitySMOImpl.auditEnterCommunity(pd);
    }

    public IAuditEnterCommunitySMO getAuditEnterCommunitySMOImpl() {
        return auditEnterCommunitySMOImpl;
    }

    public void setAuditEnterCommunitySMOImpl(IAuditEnterCommunitySMO auditEnterCommunitySMOImpl) {
        this.auditEnterCommunitySMOImpl = auditEnterCommunitySMOImpl;
    }

}
