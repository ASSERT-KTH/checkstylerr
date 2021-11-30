/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.manager.flow;
import com.hlj.proj.data.common.paging.Pagenation;
import com.hlj.proj.data.dao.mybatis.dao.flow.ScfFlowDefinitionDao;
import com.hlj.proj.data.pojo.flow.ScfFlowDefinition;
import com.hlj.proj.data.pojo.flow.ScfFlowDefinitionPage;
import com.hlj.proj.data.pojo.flow.ScfFlowDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("scfFlowDefinitionManager")
public class ScfFlowDefinitionManager {

	@Autowired
	@Qualifier("scfFlowDefinitionDao")
	private ScfFlowDefinitionDao scfFlowDefinitionDao;

	public ScfFlowDefinitionDao getDao() {
		return this.scfFlowDefinitionDao;
	}

	public int save(ScfFlowDefinition scfFlowDefinition) {
		int cnt = 0;
		if (scfFlowDefinition.getId() == null) {
			cnt = scfFlowDefinitionDao.insertSelective(scfFlowDefinition);
		} else {
			cnt = scfFlowDefinitionDao.updateByPrimaryKeySelective(scfFlowDefinition);
		}
		return cnt;
	}

	public int update(ScfFlowDefinition scfFlowDefinition) {
		return scfFlowDefinitionDao.updateByPrimaryKey(scfFlowDefinition);
	}

	public int updateSelective(ScfFlowDefinition scfFlowDefinition) {
		return scfFlowDefinitionDao.updateByPrimaryKeySelective(scfFlowDefinition);
	}

	public int insert(ScfFlowDefinition scfFlowDefinition) {
		return scfFlowDefinitionDao.insert(scfFlowDefinition);
	}

	public int insertSelective(ScfFlowDefinition scfFlowDefinition) {
		return scfFlowDefinitionDao.insertSelective(scfFlowDefinition);
	}

	public int batchInsert(List<ScfFlowDefinition> list){
		return scfFlowDefinitionDao.batchInsert(list);
	}

	public ScfFlowDefinition findById(long id) {
		return scfFlowDefinitionDao.selectByPrimaryKey(id);
	}

	public ScfFlowDefinition findByQueryContion(ScfFlowDefinitionQuery query) {
		List<ScfFlowDefinition> list = scfFlowDefinitionDao.selectByExample(query);
		if(null!=list && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	public List<ScfFlowDefinition> queryList(ScfFlowDefinitionQuery query) {
		return scfFlowDefinitionDao.selectByExample(query);
	}

	public int deleteById(long id) {
		return scfFlowDefinitionDao.deleteByPrimaryKey(id);
	}

	public int delete(ScfFlowDefinitionQuery query) {
		return scfFlowDefinitionDao.deleteByExample(query);
	}

	public ScfFlowDefinitionPage queryPageList(ScfFlowDefinitionQuery query) {
		ScfFlowDefinitionPage scfFlowDefinitionPage = new ScfFlowDefinitionPage();
		Integer itemCount = scfFlowDefinitionDao.countByExample(query);
		query.setItemCount(itemCount);

		if (itemCount == 0) {
			scfFlowDefinitionPage.setValues(null);
		} else {
			scfFlowDefinitionPage.setValues(scfFlowDefinitionDao.selectPageByExample(query));
		}

		scfFlowDefinitionPage.setPagenation(new Pagenation(query.getPageNo(), query.getPageSize(), query.getItemCount()));
		return scfFlowDefinitionPage;
	}

}
