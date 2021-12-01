package com.java110.user.bmo.staffAppAuth.impl;

import com.java110.core.annotation.Java110Transactional;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.staffAppAuth.StaffAppAuthDto;
import com.java110.intf.user.IStaffAppAuthInnerServiceSMO;
import com.java110.po.staffAppAuth.StaffAppAuthPo;
import com.java110.user.bmo.staffAppAuth.IUpdateStaffAppAuthBMO;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("updateStaffAppAuthBMOImpl")
public class UpdateStaffAppAuthBMOImpl implements IUpdateStaffAppAuthBMO {

    @Autowired
    private IStaffAppAuthInnerServiceSMO staffAppAuthInnerServiceSMOImpl;

    /**
     * @param staffAppAuthPo
     * @return 订单服务能够接受的报文
     */
    @Java110Transactional
    public ResponseEntity<String> update(StaffAppAuthPo staffAppAuthPo) {

        StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
        staffAppAuthDto.setAppType(staffAppAuthPo.getAppType());
        staffAppAuthDto.setStaffId(staffAppAuthPo.getStaffId());
        staffAppAuthDto.setStoreId(staffAppAuthPo.getStoreId());
        staffAppAuthDto.setOpenName(staffAppAuthPo.getOpenName());
        int count = staffAppAuthInnerServiceSMOImpl.queryStaffAppAuthsCount(staffAppAuthDto);
        int flag = 0;
        if (count > 0) {
            flag = staffAppAuthInnerServiceSMOImpl.updateStaffAppAuth(staffAppAuthPo);
        } else {
            staffAppAuthPo.setAuId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_auId));
            flag = staffAppAuthInnerServiceSMOImpl.saveStaffAppAuth(staffAppAuthPo);
        }

        if (flag > 0) {
            return ResultVo.createResponseEntity(ResultVo.CODE_OK, "保存成功");
        }

        return ResultVo.createResponseEntity(ResultVo.CODE_ERROR, "保存失败");
    }

}
