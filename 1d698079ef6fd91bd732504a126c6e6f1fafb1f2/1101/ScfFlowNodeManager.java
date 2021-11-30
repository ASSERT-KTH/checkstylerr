/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.manager.flow;

import com.hlj.proj.data.common.paging.Pagenation;
import com.hlj.proj.data.dao.mybatis.dao.flow.ScfFlowNodeDao;
import com.hlj.proj.data.pojo.flow.ScfFlowNode;
import com.hlj.proj.data.pojo.flow.ScfFlowNodePage;
import com.hlj.proj.data.pojo.flow.ScfFlowNodeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("scfFlowNodeManager")
public class ScfFlowNodeManager {

	@Autowired
	@Qualifier("scfFlowNodeDao")
	private ScfFlowNodeDao scfFlowNodeDao;

	public ScfFlowNodeDao getDao() {
		return this.scfFlowNodeDao;
	}

	public int save(ScfFlowNode scfFlowNode) {
		int cnt = 0;
		if (scfFlowNode.getId() == null) {
			cnt = scfFlowNodeDao.insertSelective(scfFlowNode);
		} else {
			cnt = scfFlowNodeDao.updateByPrimaryKeySelective(scfFlowNode);
		}
		return cnt;
	}

	public int update(ScfFlowNode scfFlowNode) {
		return scfFlowNodeDao.updateByPrimaryKey(scfFlowNode);
	}

	public int updateSelective(ScfFlowNode scfFlowNode) {
		return scfFlowNodeDao.updateByPrimaryKeySelective(scfFlowNode);
	}

	public int insert(ScfFlowNode scfFlowNode) {
		return scfFlowNodeDao.insert(scfFlowNode);
	}

	public int insertSelective(ScfFlowNode scfFlowNode) {
		return scfFlowNodeDao.insertSelective(scfFlowNode);
	}

	public int batchInsert(List<ScfFlowNode> list){
		return scfFlowNodeDao.batchInsert(list);
	}

	public ScfFlowNode findById(long id) {
		return scfFlowNodeDao.selectByPrimaryKey(id);
	}

	public ScfFlowNode findByQueryContion(ScfFlowNodeQuery query) {
		List<ScfFlowNode> list = scfFlowNodeDao.selectByExample(query);
		if(null!=list && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	public List<ScfFlowNode> queryList(ScfFlowNodeQuery query) {
		return scfFlowNodeDao.selectByExample(query);
	}

	public int deleteById(long id) {
		return scfFlowNodeDao.deleteByPrimaryKey(id);
	}

	public int delete(ScfFlowNodeQuery query) {
		return scfFlowNodeDao.deleteByExample(query);
	}

	public ScfFlowNodePage queryPageList(ScfFlowNodeQuery query) {
		ScfFlowNodePage scfFlowNodePage = new ScfFlowNodePage();
		Integer itemCount = scfFlowNodeDao.countByExample(query);
		query.setItemCount(itemCount);

		if (itemCount == 0) {
			scfFlowNodePage.setValues(null);
		} else {
			scfFlowNodePage.setValues(scfFlowNodeDao.selectPageByExample(query));
		}

		scfFlowNodePage.setPagenation(new Pagenation(query.getPageNo(), query.getPageSize(), query.getItemCount()));
		return scfFlowNodePage;
	}

}
