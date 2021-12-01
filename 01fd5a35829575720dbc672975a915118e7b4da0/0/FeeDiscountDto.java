package com.java110.dto.feeDiscount;

import com.java110.dto.PageDto;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName FloorDto
 * @Description 费用折扣数据层封装
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
public class FeeDiscountDto extends PageDto implements Serializable {

    private String discountName;
private String discountDesc;
private String discountType;
private String discountId;
private String communityId;
private String ruleId;


    private Date createTime;

    private String statusCd = "0";


    public String getDiscountName() {
        return discountName;
    }
public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }
public String getDiscountDesc() {
        return discountDesc;
    }
public void setDiscountDesc(String discountDesc) {
        this.discountDesc = discountDesc;
    }
public String getDiscountType() {
        return discountType;
    }
public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }
public String getDiscountId() {
        return discountId;
    }
public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }
public String getCommunityId() {
        return communityId;
    }
public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }
public String getRuleId() {
        return ruleId;
    }
public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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
