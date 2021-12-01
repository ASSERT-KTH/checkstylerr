package com.java110.fee.bmo.feeManualCollectionDetail.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.fee.bmo.feeManualCollectionDetail.IUpdateFeeManualCollectionDetailBMO;
import com.java110.intf.IFeeManualCollectionDetailInnerServiceSMO;
import com.java110.po.feeManualCollectionDetail.FeeManualCollectionDetailPo;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("updateFeeManualCollectionDetailBMOImpl")
public class UpdateFeeManualCollectionDetailBMOImpl implements IUpdateFeeManualCollectionDetailBMO {

    @Autowired
    private IFeeManualCollectionDetailInnerServiceSMO feeManualCollectionDetailInnerServiceSMOImpl;

    /**
     * @param feeManualCollectionDetailPo
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> update(FeeManualCollectionDetailPo feeManualCollectionDetailPo) {

        int flag = feeManualCollectionDetailInnerServiceSMOImpl.updateFeeManualCollectionDetail(feeManualCollectionDetailPo);

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
