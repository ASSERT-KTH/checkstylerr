/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.demo.query;

import com.hlj.proj.data.common.query.PagingQuery;
import lombok.Data;
/**
 * @author zhangyujin
 * @ClassName: DemoEntityQuery
 * @date 2099/1/1
 * @Description: DemoEntityQuery
 */
@Data
public class DemoEntityQuery extends PagingQuery {
	private static final long serialVersionUID = 1L;

	public DemoEntityQuery(){
		super(1, 5);
	}

	public DemoEntityQuery(int pageNo, int pageSize){
		super(pageNo, pageSize);
	}

	/**  */
	private String name;
	/** 手机号 */
	private String phone;
	/** 邮箱 */
	private String email;
	/**  */
	private Integer age;
	/** 10可用，99删除 */
	private String delFlag;
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
