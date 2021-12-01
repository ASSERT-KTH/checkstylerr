package com.java110.fee.api;

import com.java110.dto.meterWater.MeterWaterDto;
import com.java110.fee.bmo.meterWater.IQueryPreMeterWater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/meterWater")
public class MeterWaterApi {


    @Autowired
    private IQueryPreMeterWater queryPreMeterWaterImpl;


    /**
     * 查询上期度数信息
     *
     * @param communityId 小区ID
     * @return
     * @serviceCode /meterWater/queryPreMeterWater
     * @path /app/meterWater/queryPreMeterWater
     */
    @RequestMapping(value = "/queryPreMeterWater", method = RequestMethod.GET)
    public ResponseEntity<String> queryPreMeterWater(@RequestParam(value = "communityId") String communityId,
                                                     @RequestParam(value = "objId") String objId,
                                                     @RequestParam(value = "objType") String objType,
                                                     @RequestParam(value = "roomNum" ,required = false) String roomNum) {
        MeterWaterDto meterWaterDto = new MeterWaterDto();
        meterWaterDto.setObjId(objId);
        meterWaterDto.setObjType(objType);
        meterWaterDto.setPage(1);
        meterWaterDto.setRow(1);
        meterWaterDto.setCommunityId(communityId);
        return queryPreMeterWaterImpl.query(meterWaterDto,roomNum);
    }
}
