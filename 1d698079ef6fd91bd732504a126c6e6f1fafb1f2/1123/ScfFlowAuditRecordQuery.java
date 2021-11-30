/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.pojo.flow;

import com.hlj.proj.data.common.query.PagingQuery;
import lombok.Data;

import java.util.List;

/**
 * @author zhangyujin
 * @ClassName: ScfFlowAuditRecordQuery
 * @date 2099/1/1
 * @Description: ScfFlowAuditRecordQuery
 */
@Data
public class ScfFlowAuditRecordQuery extends PagingQuery {
	private static final long serialVersionUID = 1L;

	public ScfFlowAuditRecordQuery(){
		super(1, 10);
	}

	public ScfFlowAuditRecordQuery(int pageNo, int pageSize){
		super(pageNo, pageSize);
	}

	/** 审批附件 */
	private String refFileIds;
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
	/** 执行人 */
	private Long optUser;
	/** 执行人真实名称 */
	private String optUserName;
	/** 执行时间 */
	private java.util.Date optTime;
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

	/**
	 * 额外
	 */
	private Long id ;

	private List<Long> roleIds;


}
