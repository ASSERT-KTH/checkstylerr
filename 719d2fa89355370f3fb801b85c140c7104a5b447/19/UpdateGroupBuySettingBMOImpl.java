package com.java110.goods.bmo.groupBuySetting.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.groupBuyBatch.GroupBuyBatchDto;
import com.java110.goods.bmo.groupBuySetting.IUpdateGroupBuySettingBMO;

import com.java110.intf.goods.IGroupBuyBatchInnerServiceSMO;
import com.java110.intf.goods.IGroupBuySettingInnerServiceSMO;
import com.java110.po.groupBuyBatch.GroupBuyBatchPo;
import com.java110.po.groupBuySetting.GroupBuySettingPo;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("updateGroupBuySettingBMOImpl")
public class UpdateGroupBuySettingBMOImpl implements IUpdateGroupBuySettingBMO {

    @Autowired
    private IGroupBuySettingInnerServiceSMO groupBuySettingInnerServiceSMOImpl;
    @Autowired
    private IGroupBuyBatchInnerServiceSMO groupBuyBatchInnerServiceSMOImpl;

    /**
     * @param groupBuySettingPo
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> update(GroupBuySettingPo groupBuySettingPo) {

        int flag = groupBuySettingInnerServiceSMOImpl.updateGroupBuySetting(groupBuySettingPo);

        if (flag < 1) {
            return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
        }

        GroupBuyBatchDto groupBuyBatchDto = new GroupBuyBatchDto();
        groupBuyBatchDto.setStoreId(groupBuySettingPo.getStoreId());
        groupBuyBatchDto.setSettingId(groupBuySettingPo.getSettingId());
        int count = groupBuyBatchInnerServiceSMOImpl.queryGroupBuyBatchsCount(groupBuyBatchDto);

        if (count < 1) {
            GroupBuyBatchPo groupBuyBatchPo = new GroupBuyBatchPo();
            groupBuyBatchPo.setStoreId(groupBuySettingPo.getStoreId());
            groupBuyBatchPo.setSettingId(groupBuySettingPo.getSettingId());
            groupBuyBatchPo.setBatchId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_batchId));
            groupBuyBatchPo.setBatchStartTime(groupBuySettingPo.getStartTime());
            groupBuyBatchPo.setCurBatch("Y");
            groupBuyBatchPo.setBatchEndTime(groupBuySettingPo.getEndTime());
            flag = groupBuyBatchInnerServiceSMOImpl.saveGroupBuyBatch(groupBuyBatchPo);

            if (flag < 0) {
                return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
            }
        }
        return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");

    }

}
