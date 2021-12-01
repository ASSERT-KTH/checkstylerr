package com.java110.goods.bmo.productAttr.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.goods.bmo.productAttr.ISaveProductAttrBMO;
import com.java110.intf.goods.IProductAttrInnerServiceSMO;
import com.java110.po.productAttr.ProductAttrPo;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("saveProductAttrBMOImpl")
public class SaveProductAttrBMOImpl implements ISaveProductAttrBMO {

    @Autowired
    private IProductAttrInnerServiceSMO productAttrInnerServiceSMOImpl;

    /**
     * 添加小区信息
     *
     * @param productAttrPo
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> save(ProductAttrPo productAttrPo) {

        productAttrPo.setAttrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_attrId));
        int flag = productAttrInnerServiceSMOImpl.saveProductAttr(productAttrPo);

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
