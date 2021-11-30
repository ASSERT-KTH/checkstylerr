/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.pojo.flow;

import com.hlj.proj.data.common.query.PagingQuery;
import lombok.Data;
/**
 * @author zhangyujin
 * @ClassName: FlowAuditRecordLogQuery
 * @date 2099/1/1
 * @Description: FlowAuditRecordLogQuery
 */
@Data
public class FlowAuditRecordLogQuery extends PagingQuery {
	private static final long serialVersionUID = 1L;

	public FlowAuditRecordLogQuery(){
		super(1, 10);
	}

	public FlowAuditRecordLogQuery(int pageNo, int pageSize){
		super(pageNo, pageSize);
	}

	/** 审批附件 */
	private String refFileIds;
	/** 审批记录表主键 */
	private Long refFlowAuditRecordId;
	/** 流程实例流水号 */
	private String instantsNo;
	/** 流程步骤 */
	private Integer sept;
	/** 节点编码 */
	private String flowCode;
	/** 节点名称 */
	private String flowName;
	/** 节点编号 */
	private String nodeCode;
	/** 节点名称 */
	private String nodeName;
	/** 审核步骤 */
	private Integer auditSept;
	/** 审批内容 */
	private String auditData;
	/** 状态 */
	private String status;
	/** 审批意见 */
	private String auditMessage;
	/** 创建人 */
	private Long createUser;
	/** 创建人名称 */
	private String createName;


	/** 计算总记录数 */
	public int calcItemCount(Object t) {
		return 0;
	}



}
