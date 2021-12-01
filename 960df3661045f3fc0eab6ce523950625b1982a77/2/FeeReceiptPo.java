package com.java110.po.feeReceipt;

import java.io.Serializable;
import java.util.Date;

public class FeeReceiptPo implements Serializable {

    private String amount;
private String objId;
private String remark;
private String statusCd = "0";
private String objName;
private String communityId;
private String receiptId;
private String objType;
public String getAmount() {
        return amount;
    }
public void setAmount(String amount) {
        this.amount = amount;
    }
public String getObjId() {
        return objId;
    }
public void setObjId(String objId) {
        this.objId = objId;
    }
public String getRemark() {
        return remark;
    }
public void setRemark(String remark) {
        this.remark = remark;
    }
public String getStatusCd() {
        return statusCd;
    }
public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }
public String getObjName() {
        return objName;
    }
public void setObjName(String objName) {
        this.objName = objName;
    }
public String getCommunityId() {
        return communityId;
    }
public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }
public String getReceiptId() {
        return receiptId;
    }
public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }
public String getObjType() {
        return objType;
    }
public void setObjType(String objType) {
        this.objType = objType;
    }



}
