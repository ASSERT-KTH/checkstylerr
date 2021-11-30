/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.dao.flow;

import java.util.List;

import com.hlj.proj.data.dao.mybatis.dao.BaseDao;
import com.hlj.proj.data.pojo.flow.FlowRefAuditorEvent;
import com.hlj.proj.data.pojo.flow.FlowRefAuditorEventQuery;
import com.hlj.proj.data.pojo.flow.ScfFlowNode;
import org.springframework.stereotype.Repository;

/**
 * @author zhangyujin
 * @ClassName: FlowRefAuditorEventDao
 * @date 2099/1/1
 * @Description: FlowRefAuditorEventDao
 */
@Repository("flowRefAuditorEventDao")
public class FlowRefAuditorEventDao extends BaseDao {

	public int countByExample(FlowRefAuditorEventQuery example) {
		return super.getSqlSession().selectOne("FlowRefAuditorEventMapper.countByExample", example);
	}

	public int deleteByExample(FlowRefAuditorEventQuery example) {
		return super.getSqlSession().delete("FlowRefAuditorEventMapper.deleteByExample", example);
	}

	public int deleteByPrimaryKey(long id) {
		return super.getSqlSession().delete("FlowRefAuditorEventMapper.deleteByPrimaryKey", id);
	}

	public int insert(FlowRefAuditorEvent record) {
		return super.getSqlSession().insert("FlowRefAuditorEventMapper.insert", record);
	}

	public int insertSelective(FlowRefAuditorEvent record) {
		return super.getSqlSession().insert("FlowRefAuditorEventMapper.insertSelective", record);
	}

	public int batchInsert(List<FlowRefAuditorEvent> list) {
		return super.batchInsert("FlowRefAuditorEventMapper.insertSelective", list);
	}

	public List<FlowRefAuditorEvent> selectByExample(FlowRefAuditorEventQuery example) {
		return super.getSqlSession().selectList("FlowRefAuditorEventMapper.selectByExample", example);
	}

	public List<FlowRefAuditorEvent> selectPageByExample(FlowRefAuditorEventQuery example) {
		return super.getSqlSession().selectList("FlowRefAuditorEventMapper.selectPageByExample", example);
	}

	public FlowRefAuditorEvent selectByPrimaryKey(long id) {
		return super.getSqlSession().selectOne("FlowRefAuditorEventMapper.selectByPrimaryKey", id);
	}

	public int updateByPrimaryKeySelective(FlowRefAuditorEvent record) {
		return super.getSqlSession().update("FlowRefAuditorEventMapper.updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(FlowRefAuditorEvent record) {
		return super.getSqlSession().update("FlowRefAuditorEventMapper.updateByPrimaryKey", record);
	}

    public int batchUpdate(List<FlowRefAuditorEvent> list) {
		return super.batchUpdate("FlowRefAuditorEventMapper.updateByPrimaryKeySelective", list);
	}


}
