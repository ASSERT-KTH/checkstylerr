package com.java110.dto.meterWater;

import com.java110.dto.PageDto;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName FloorDto
 * @Description 水电费数据层封装
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
public class MeterWaterDto extends PageDto implements Serializable {

    private String remark;
private String curReadingTime;
private String waterId;
private String curDegrees;
private String meterType;
private String preDegrees;
private String objId;
private String preReadingTime;
private String communityId;
private String objType;


    private Date createTime;

    private String statusCd = "0";


    public String getRemark() {
        return remark;
    }
public void setRemark(String remark) {
        this.remark = remark;
    }
public String getCurReadingTime() {
        return curReadingTime;
    }
public void setCurReadingTime(String curReadingTime) {
        this.curReadingTime = curReadingTime;
    }
public String getWaterId() {
        return waterId;
    }
public void setWaterId(String waterId) {
        this.waterId = waterId;
    }
public String getCurDegrees() {
        return curDegrees;
    }
public void setCurDegrees(String curDegrees) {
        this.curDegrees = curDegrees;
    }
public String getMeterType() {
        return meterType;
    }
public void setMeterType(String meterType) {
        this.meterType = meterType;
    }
public String getPreDegrees() {
        return preDegrees;
    }
public void setPreDegrees(String preDegrees) {
        this.preDegrees = preDegrees;
    }
public String getObjId() {
        return objId;
    }
public void setObjId(String objId) {
        this.objId = objId;
    }
public String getPreReadingTime() {
        return preReadingTime;
    }
public void setPreReadingTime(String preReadingTime) {
        this.preReadingTime = preReadingTime;
    }
public String getCommunityId() {
        return communityId;
    }
public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }
public String getObjType() {
        return objType;
    }
public void setObjType(String objType) {
        this.objType = objType;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }
}
