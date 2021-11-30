/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.pojo.flow;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScfFlowNode implements Serializable{
	private static final long serialVersionUID = 1L;

	/** 主键 */
	private Long id;
	/** 节点编号 */
	private String nodeCode;
	/** 节点名称 */
	private String nodeName;
	/** 流程节点或者审核节点 */
	private String nodeType;
	/** 审批业务业务类型 */
	private java.lang.String auditBusinessType;
	/** 节点内部定义（目前主要是审核节点使用） */
	private String auditors;
	/**抄送人*/
	private String copyTo;
	/** 状态 */
	private String status;
	/** 创建人 */
	private Long createUser;
	/** 创建人名称 */
	private String createName;
	/** 创建时间 */
	private java.util.Date createTime;
	/** 更新人 */
	private Long updateUser;
	/** 更新人名称 */
	private String updateName;
	/** 更新时间 */
	private java.util.Date updateTime;

}
