package com.java110.goods.bmo.groupBuyProduct;

import com.java110.po.groupBuyProduct.GroupBuyProductPo;
import org.springframework.http.ResponseEntity;

public interface IDeleteGroupBuyProductBMO {


    /**
     * 修改拼团产品
     * add by wuxw
     *
     * @param groupBuyProductPo
     * @return
     */
    ResponseEntity<String> delete(GroupBuyProductPo groupBuyProductPo);


}
