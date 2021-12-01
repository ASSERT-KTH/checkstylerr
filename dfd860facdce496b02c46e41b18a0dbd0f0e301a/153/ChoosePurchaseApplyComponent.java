package com.java110.front.components.purchaseApply;


import com.java110.core.context.IPageData;
import com.java110.front.smo.purchaseApply.IListPurchaseApplysSMO;
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
@Component("choosePurchaseApply")
public class ChoosePurchaseApplyComponent {

    @Autowired
    private IListPurchaseApplysSMO listPurchaseApplysSMOImpl;

    /**
     * 查询应用列表
     *
     * @param pd 页面数据封装
     * @return 返回 ResponseEntity 对象
     */
    public ResponseEntity<String> list(IPageData pd) {
        return listPurchaseApplysSMOImpl.listPurchaseApplys(pd);
    }

    public IListPurchaseApplysSMO getListPurchaseApplysSMOImpl() {
        return listPurchaseApplysSMOImpl;
    }

    public void setListPurchaseApplysSMOImpl(IListPurchaseApplysSMO listPurchaseApplysSMOImpl) {
        this.listPurchaseApplysSMOImpl = listPurchaseApplysSMOImpl;
    }
}
