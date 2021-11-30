/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.manager.flow;

import java.util.List;

import com.hlj.proj.data.common.paging.Pagenation;
import com.hlj.proj.data.dao.mybatis.dao.flow.FlowRefAuditorEventDao;
import com.hlj.proj.data.pojo.flow.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
/**
 * @author zhangyujin
 * @ClassName: FlowRefAuditorEventManager
 * @date 2099/1/1
 * @Description: FlowRefAuditorEventManager
 */
@Component("flowRefAuditorEventManager")
public class FlowRefAuditorEventManager {

	@Autowired
	@Qualifier("flowRefAuditorEventDao")
	private FlowRefAuditorEventDao flowRefAuditorEventDao;

	public FlowRefAuditorEventDao getDao() {
		return this.flowRefAuditorEventDao;
	}

	public int save(FlowRefAuditorEvent flowRefAuditorEvent) {
		int cnt = 0;
		if (flowRefAuditorEvent.getId() == null) {
			cnt = flowRefAuditorEventDao.insertSelective(flowRefAuditorEvent);
		} else {
			cnt = flowRefAuditorEventDao.updateByPrimaryKeySelective(flowRefAuditorEvent);
		}
		return cnt;
	}

	public int update(FlowRefAuditorEvent flowRefAuditorEvent) {
		return flowRefAuditorEventDao.updateByPrimaryKey(flowRefAuditorEvent);
	}

	public int updateSelective(FlowRefAuditorEvent flowRefAuditorEvent) {
		return flowRefAuditorEventDao.updateByPrimaryKeySelective(flowRefAuditorEvent);
	}


	public int batchUpdate(List<FlowRefAuditorEvent> list) {
		return flowRefAuditorEventDao.batchUpdate( list);
	}

	public int insert(FlowRefAuditorEvent flowRefAuditorEvent) {
		return flowRefAuditorEventDao.insert(flowRefAuditorEvent);
	}

	public int insertSelective(FlowRefAuditorEvent flowRefAuditorEvent) {
		return flowRefAuditorEventDao.insertSelective(flowRefAuditorEvent);
	}

	public int batchInsert(List<FlowRefAuditorEvent> list){
		return flowRefAuditorEventDao.batchInsert(list);
	}

	public FlowRefAuditorEvent findById(long id) {
		return flowRefAuditorEventDao.selectByPrimaryKey(id);
	}

	public FlowRefAuditorEvent findByQueryContion(FlowRefAuditorEventQuery query) {
		List<FlowRefAuditorEvent> list = flowRefAuditorEventDao.selectByExample(query);
		if(null!=list && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	public List<FlowRefAuditorEvent> queryList(FlowRefAuditorEventQuery query) {
		return flowRefAuditorEventDao.selectByExample(query);
	}

	public int deleteById(long id) {
		return flowRefAuditorEventDao.deleteByPrimaryKey(id);
	}

	public int delete(FlowRefAuditorEventQuery query) {
		return flowRefAuditorEventDao.deleteByExample(query);
	}

	public FlowRefAuditorEventPage queryPageList(FlowRefAuditorEventQuery query) {
		FlowRefAuditorEventPage flowRefAuditorEventPage = new FlowRefAuditorEventPage();
		Integer itemCount = flowRefAuditorEventDao.countByExample(query);
		query.setItemCount(itemCount);

		if (itemCount == 0) {
			flowRefAuditorEventPage.setValues(null);
		} else {
			flowRefAuditorEventPage.setValues(flowRefAuditorEventDao.selectPageByExample(query));
		}

		flowRefAuditorEventPage.setPagenation(new Pagenation(query.getPageNo(), query.getPageSize(), query.getItemCount()));
		return flowRefAuditorEventPage;
	}




}
