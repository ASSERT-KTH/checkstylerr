/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.pojo.flow;

import com.hlj.proj.data.common.query.PagingQuery;
import lombok.Data;


@Data
public class ScfFlowRecordQuery extends PagingQuery {
	private static final long serialVersionUID = 1L;

	public ScfFlowRecordQuery(){
		super(1, 10);
	}

	public ScfFlowRecordQuery(int pageNo, int pageSize){
		super(pageNo, pageSize);
	}

	/** 流程实例流水号 */
	private String instantsNo;
	/** 节点编码 */
	private String flowCode;
	/** 节点名称 */
	private String flowName;
	/** 流程步骤 */
	private Integer sept;
	/** 节点编号 */
	private String nodeCode;
	/** 节点名称 */
	private String nodeName;
	/** 创建人 */
	private Long createUser;
	/** 创建人名称 */
	private String createName;
	/** 状态 */
	private String status;

	/** 计算总记录数 */
	public int calcItemCount(Object t) {
		return 0;
	}

}
