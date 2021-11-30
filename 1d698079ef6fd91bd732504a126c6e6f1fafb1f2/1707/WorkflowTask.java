package com.duodian.admore.entity.db.workflow;

import com.duodian.admore.enums.workflow.EnumWorkflowTaskStatus;
import com.duodian.admore.enums.workflow.EnumWorkflowTaskType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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
@Table(name = "workflow_task")
public class WorkflowTask implements Serializable {

    private static final long serialVersionUID = 4198682330135502141L;

    public static final int SPECIALMONEYENDSHOWFLAG_NO = 0;
    public static final int SPECIALMONEYENDSHOWFLAG_YES = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer taskType;       //EnumWorkflowTaskType

    private Long cuserId;       //申请人

    private Long pid;           //申请类型对应的 具体申请记录的ID 比如预开发票申请 对应的 就是预开发票的ID

    @Column(nullable = false,columnDefinition = "int DEFAULT 1")
    private Integer allowType;  //助手管理无Long类型的id,这里添加类型 0-代表助手管理审核 , 1-代表其他的，默认为1。

    private String trackId;       //助手管理审核信息中 唯一标识

    private Integer status;     //审批状态 EnumWorkflowTaskStatus

    private Integer cuserType;  //申请人类型 EnumWorkflowTaskCuserType

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = false)
    private Date cdate;                      //创建时间

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = true)
    private Date udate;                      //最后处理时间

    @Transient
    private String taskTypeDesc;

    @Transient
    private String cuserName;

    @Transient
    private String statusDesc;

    @Transient
    private String remark_1;    //摘要
    @Transient
    private String remark_2;
    @Transient
    private String remark_3;

    @Transient
    private Integer specialMoneyEndShowFlag;        //0-不显示结束  1-显示结束


    public WorkflowTask() {
    }

    public WorkflowTask(Integer taskType, Long cuserId, Integer cuserType, Long pid, Integer status) {
        this.taskType = taskType;
        this.cuserId = cuserId;
        this.cuserType = cuserType;
        this.pid = pid;
        this.status = status;
    }

    public WorkflowTask(Integer taskType, Long cuserId, Integer cuserType, String trackId,Integer allowType, Integer status) {
        this.taskType = taskType;
        this.cuserId = cuserId;
        this.cuserType = cuserType;
        this.trackId = trackId;
        this.allowType = allowType;
        this.status = status;
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

    public Long getCuserId() {
        return cuserId;
    }

    public void setCuserId(Long cuserId) {
        this.cuserId = cuserId;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
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

    public String getCuserName() {
        return cuserName;
    }

    public void setCuserName(String cuserName) {
        this.cuserName = cuserName;
    }

    public String getStatusDesc() {
        return EnumWorkflowTaskStatus.getDes(this.status);
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getRemark_1() {
        return remark_1;
    }

    public void setRemark_1(String remark_1) {
        this.remark_1 = remark_1;
    }

    public String getRemark_2() {
        return remark_2;
    }

    public void setRemark_2(String remark_2) {
        this.remark_2 = remark_2;
    }

    public String getRemark_3() {
        return remark_3;
    }

    public void setRemark_3(String remark_3) {
        this.remark_3 = remark_3;
    }

    public Integer getCuserType() {
        return cuserType;
    }

    public void setCuserType(Integer cuserType) {
        this.cuserType = cuserType;
    }

    public Integer getSpecialMoneyEndShowFlag() {
        return specialMoneyEndShowFlag;
    }

    public void setSpecialMoneyEndShowFlag(Integer specialMoneyEndShowFlag) {
        this.specialMoneyEndShowFlag = specialMoneyEndShowFlag;
    }


    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public Integer getAllowType() {
        return allowType;
    }

    public void setAllowType(Integer allowType) {
        this.allowType = allowType;
    }
}
