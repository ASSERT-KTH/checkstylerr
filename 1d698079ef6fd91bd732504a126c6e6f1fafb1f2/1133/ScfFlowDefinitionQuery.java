/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.pojo.flow;

import com.hlj.proj.data.common.query.PagingQuery;
import lombok.Data;

@Data
public class ScfFlowDefinitionQuery extends PagingQuery {
	private static final long serialVersionUID = 1L;

	public ScfFlowDefinitionQuery(){
		super(1, 10);
	}

	public ScfFlowDefinitionQuery(int pageNo, int pageSize){
		super(pageNo, pageSize);
	}

	/** 节点编码 */
	private String flowCode;
	/** 节点名称 */
	private String flowName;
	/** 节点内部定义（目前主要是审核节点使用） */
	private String flowDefinition;
	/** 状态 */
	private String status;
	/** 创建人 */
	private Long createUser;
	/** 创建人名称 */
	private String createName;
	/** 更新人 */
	private Long updateUser;
	/** 更新人名称 */
	private String updateName;

	/** 计算总记录数 */
	public int calcItemCount(Object t) {
		return 0;
	}



}
