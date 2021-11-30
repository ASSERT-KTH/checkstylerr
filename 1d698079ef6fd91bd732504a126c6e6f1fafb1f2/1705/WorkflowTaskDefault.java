package com.duodian.admore.entity.db.workflow;

import com.duodian.admore.entity.db.admin.SysAdminUser;
import com.duodian.admore.enums.EnumYesNo;
import com.duodian.admore.enums.workflow.EnumWorkflowTaskType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 类描述：
 * 创建人：lishiwei
 * 创建时间：2017/2/16
 * 修改人：
 * 修改时间：
 * 修改备注：
 *
 * @version 1.0.0
 */
@Entity
@Table(name = "workflow_task_default",
        indexes = {
                @Index(name = "type_uniq", columnList = "taskType", unique = true)
        }
        )
public class WorkflowTaskDefault implements Serializable {

    private static final long serialVersionUID = 4198682330135502141L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer taskType;       //EnumWorkflowTaskType

    private Long lastAdmId;       //最后操作人

    private Integer status;     //状态  0-禁用  1-可用 EnumYesNo

    private Integer modifyFlag;     //申请时是否可修改审批人及抄送人（可以增加其他审批人或抄送人但是不能去掉默认的）  0-不可修改  1-可修改 EnumYesNo

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = false)
    private Date cdate;                      //创建时间

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = true)
    private Date udate;                      //最后处理时间

    @Transient
    private String taskTypeDesc;

    @Transient
    private String lastAdmName;

    @Transient
    private String statusDesc;

    @Transient
    private String modifyFlagDesc;

    @Transient
    private List<SysAdminUser> apprList;
    @Transient
    private List<SysAdminUser> ccList;

    public WorkflowTaskDefault() {
    }

    public WorkflowTaskDefault(Integer taskType, Long lastAdmId, Integer status, Integer modifyFlag) {
        this.taskType = taskType;
        this.lastAdmId = lastAdmId;
        this.status = status;
        this.modifyFlag = modifyFlag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Long getLastAdmId() {
        return lastAdmId;
    }

    public void setLastAdmId(Long lastAdmId) {
        this.lastAdmId = lastAdmId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    public Date getUdate() {
        return udate;
    }

    public void setUdate(Date udate) {
        this.udate = udate;
    }

    public String getTaskTypeDesc() {
        return EnumWorkflowTaskType.getDes(this.taskType);
    }

    public void setTaskTypeDesc(String taskTypeDesc) {
        this.taskTypeDesc = taskTypeDesc;
    }

    public String getLastAdmName() {
        return lastAdmName;
    }

    public void setLastAdmName(String lastAdmName) {
        this.lastAdmName = lastAdmName;
    }

    public String getStatusDesc() {
        if (this.status == null){
            return "未知";
        }
        if (this.status.compareTo(EnumYesNo.YES.status) == 0){
            return "可用";
        } else if (this.status.compareTo(EnumYesNo.NO.status) == 0){
            return "禁用";
        } else {
            return "未知";
        }

    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public List<SysAdminUser> getApprList() {
        return apprList;
    }

    public void setApprList(List<SysAdminUser> apprList) {
        this.apprList = apprList;
    }

    public List<SysAdminUser> getCcList() {
        return ccList;
    }

    public void setCcList(List<SysAdminUser> ccList) {
        this.ccList = ccList;
    }

    public Integer getModifyFlag() {
        return modifyFlag;
    }

    public void setModifyFlag(Integer modifyFlag) {
        this.modifyFlag = modifyFlag;
    }

    public String getModifyFlagDesc() {
        if (this.modifyFlag == null){
            return "未知";
        }
        if (this.modifyFlag.compareTo(EnumYesNo.YES.status) == 0){
            return "可修改";
        } else if (this.modifyFlag.compareTo(EnumYesNo.NO.status) == 0){
            return "不可修改";
        } else {
            return "未知";
        }
    }

    public void setModifyFlagDesc(String modifyFlagDesc) {
        this.modifyFlagDesc = modifyFlagDesc;
    }
}
