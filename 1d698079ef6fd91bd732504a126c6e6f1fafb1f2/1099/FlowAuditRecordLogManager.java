/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.manager.flow;

import java.util.List;

import com.hlj.proj.data.common.paging.Pagenation;
import com.hlj.proj.data.dao.mybatis.dao.flow.FlowAuditRecordLogDao;
import com.hlj.proj.data.pojo.flow.FlowAuditRecordLog;
import com.hlj.proj.data.pojo.flow.FlowAuditRecordLogPage;
import com.hlj.proj.data.pojo.flow.FlowAuditRecordLogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author zhangyujin
 * @ClassName: FlowAuditRecordLogManager
 * @date 2099/1/1
 * @Description: FlowAuditRecordLogManager
 */
@Component("flowAuditRecordLogManager")
public class FlowAuditRecordLogManager {

	@Autowired
	@Qualifier("flowAuditRecordLogDao")
	private FlowAuditRecordLogDao flowAuditRecordLogDao;

	public FlowAuditRecordLogDao getDao() {
		return this.flowAuditRecordLogDao;
	}

	public int save(FlowAuditRecordLog flowAuditRecordLog) {
		int cnt = 0;
		if (flowAuditRecordLog.getId() == null) {
			cnt = flowAuditRecordLogDao.insertSelective(flowAuditRecordLog);
		} else {
			cnt = flowAuditRecordLogDao.updateByPrimaryKeySelective(flowAuditRecordLog);
		}
		return cnt;
	}

	public int update(FlowAuditRecordLog flowAuditRecordLog) {
		return flowAuditRecordLogDao.updateByPrimaryKey(flowAuditRecordLog);
	}

	public int updateSelective(FlowAuditRecordLog flowAuditRecordLog) {
		return flowAuditRecordLogDao.updateByPrimaryKeySelective(flowAuditRecordLog);
	}

	public int insert(FlowAuditRecordLog flowAuditRecordLog) {
		return flowAuditRecordLogDao.insert(flowAuditRecordLog);
	}

	public int insertSelective(FlowAuditRecordLog flowAuditRecordLog) {
		return flowAuditRecordLogDao.insertSelective(flowAuditRecordLog);
	}

	public int batchInsert(List<FlowAuditRecordLog> list){
		return flowAuditRecordLogDao.batchInsert(list);
	}

	public FlowAuditRecordLog findById(long id) {
		return flowAuditRecordLogDao.selectByPrimaryKey(id);
	}

	public FlowAuditRecordLog findByQueryContion(FlowAuditRecordLogQuery query) {
		List<FlowAuditRecordLog> list = flowAuditRecordLogDao.selectByExample(query);
		if(null!=list && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	public List<FlowAuditRecordLog> queryList(FlowAuditRecordLogQuery query) {
		return flowAuditRecordLogDao.selectByExample(query);
	}

	public int deleteById(long id) {
		return flowAuditRecordLogDao.deleteByPrimaryKey(id);
	}

	public int delete(FlowAuditRecordLogQuery query) {
		return flowAuditRecordLogDao.deleteByExample(query);
	}

	public FlowAuditRecordLogPage queryPageList(FlowAuditRecordLogQuery query) {
		FlowAuditRecordLogPage flowAuditRecordLogPage = new FlowAuditRecordLogPage();
		Integer itemCount = flowAuditRecordLogDao.countByExample(query);
		query.setItemCount(itemCount);

		if (itemCount == 0) {
			flowAuditRecordLogPage.setValues(null);
		} else {
			flowAuditRecordLogPage.setValues(flowAuditRecordLogDao.selectPageByExample(query));
		}

		flowAuditRecordLogPage.setPagenation(new Pagenation(query.getPageNo(), query.getPageSize(), query.getItemCount()));
		return flowAuditRecordLogPage;
	}

}
