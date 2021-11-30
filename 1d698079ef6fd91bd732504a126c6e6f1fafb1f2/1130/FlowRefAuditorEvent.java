/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.pojo.flow;

import java.io.Serializable;
import lombok.Data;
/**
 * @author zhangyujin
 * @ClassName: FlowRefAuditorEvent
 * @date 2099/1/1
 * @Description: FlowRefAuditorEvent
 */
@Data
public class FlowRefAuditorEvent implements Serializable{
	private static final long serialVersionUID = 1L;

	/** 主键 */
	private Long id;
	/** 审批记录表主键 */
	private Long refFlowAuditRecordId;
	/** 审核类型：角色或ID */
	private String auditType;
	/** 审核对象 */
	private Long auditObject;
	/** 创建人 */
	private Long createUser;
	/** 创建人名称 */
	private String createName;
	/**是否抄送人*/
	private Boolean copy ;
	/** 创建时间 */
	private java.util.Date createTime;
	/** 更新时间 */
	private java.util.Date updateTime;

}
