package com.duodian.admore.entity.db.user;

import com.duodian.admore.enums.user.EnumOldBackGroundBalanceTranferStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_old_background_balance_trannsfer")
public class OldBackGroundBalanceTransferUser implements Serializable{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId; //创建者id

    private Long customerId;//老客户id

    private Long userId; //new客户id

    private String userName ; //新客户名称【id】邮箱

    private BigDecimal balance; //老客户金额

    private String customerName;//老客户名称

    private String remark;//备注

    private Integer auditStatus ; //EnumOldBackGroundBalanceTranferStatus

    @Transient
    private String userNickName ; //新客户真实用户名

    @Transient
    private  String userAuthName ;

    @Transient
    private  String cuserName; //创建者名称


    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable = true,updatable = false)
    private Date cdate;

    @Transient
    private String attachPics;
    @Transient
    private String attachFileNames;
    @Transient
    private String attachFileTypes;
    @Transient
    private List<OldBackGroundBalanceTransferUserAttachment> attachmentList;

    @Transient
    private String statusDesc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAttachPics() {
        return attachPics;
    }

    public void setAttachPics(String attachPics) {
        this.attachPics = attachPics;
    }

    public String getAttachFileNames() {
        return attachFileNames;
    }

    public void setAttachFileNames(String attachFileNames) {
        this.attachFileNames = attachFileNames;
    }

    public List<OldBackGroundBalanceTransferUserAttachment> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<OldBackGroundBalanceTransferUserAttachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAttachFileTypes() {
        return attachFileTypes;
    }

    public void setAttachFileTypes(String attachFileTypes) {
        this.attachFileTypes = attachFileTypes;
    }


    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserAuthName() {
        return userAuthName;
    }

    public void setUserAuthName(String userAuthName) {
        this.userAuthName = userAuthName;
    }

    public String getCuserName() {
        return cuserName;
    }

    public void setCuserName(String cuserName) {
        this.cuserName = cuserName;
    }

    public String getStatusDesc() {
        return EnumOldBackGroundBalanceTranferStatus.getDes(this.auditStatus);
    }
}
