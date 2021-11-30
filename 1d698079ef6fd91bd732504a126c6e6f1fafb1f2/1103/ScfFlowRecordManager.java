/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.manager.flow;

import com.hlj.proj.data.common.paging.Pagenation;
import com.hlj.proj.data.dao.mybatis.dao.flow.ScfFlowRecordDao;
import com.hlj.proj.data.pojo.flow.ScfFlowRecord;
import com.hlj.proj.data.pojo.flow.ScfFlowRecordPage;
import com.hlj.proj.data.pojo.flow.ScfFlowRecordQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("scfFlowRecordManager")
public class ScfFlowRecordManager {

	@Autowired
	@Qualifier("scfFlowRecordDao")
	private ScfFlowRecordDao scfFlowRecordDao;

	public ScfFlowRecordDao getDao() {
		return this.scfFlowRecordDao;
	}

	public int save(ScfFlowRecord scfFlowRecord) {
		int cnt = 0;
		if (scfFlowRecord.getId() == null) {
			cnt = scfFlowRecordDao.insertSelective(scfFlowRecord);
		} else {
			cnt = scfFlowRecordDao.updateByPrimaryKeySelective(scfFlowRecord);
		}
		return cnt;
	}

	public int update(ScfFlowRecord scfFlowRecord) {
		return scfFlowRecordDao.updateByPrimaryKey(scfFlowRecord);
	}

	public int updateSelective(ScfFlowRecord scfFlowRecord) {
		return scfFlowRecordDao.updateByPrimaryKeySelective(scfFlowRecord);
	}

	public int insert(ScfFlowRecord scfFlowRecord) {
		return scfFlowRecordDao.insert(scfFlowRecord);
	}

	public int insertSelective(ScfFlowRecord scfFlowRecord) {
		return scfFlowRecordDao.insertSelective(scfFlowRecord);
	}

	public int batchInsert(List<ScfFlowRecord> list){
		return scfFlowRecordDao.batchInsert(list);
	}

	public ScfFlowRecord findById(long id) {
		return scfFlowRecordDao.selectByPrimaryKey(id);
	}

	public ScfFlowRecord findByQueryContion(ScfFlowRecordQuery query) {
		List<ScfFlowRecord> list = scfFlowRecordDao.selectByExample(query);
		if(null!=list && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	public List<ScfFlowRecord> queryList(ScfFlowRecordQuery query) {
		return scfFlowRecordDao.selectByExample(query);
	}

	public int deleteById(long id) {
		return scfFlowRecordDao.deleteByPrimaryKey(id);
	}

	public int delete(ScfFlowRecordQuery query) {
		return scfFlowRecordDao.deleteByExample(query);
	}

	public ScfFlowRecordPage queryPageList(ScfFlowRecordQuery query) {
		ScfFlowRecordPage scfFlowRecordPage = new ScfFlowRecordPage();
		Integer itemCount = scfFlowRecordDao.countByExample(query);
		query.setItemCount(itemCount);

		if (itemCount == 0) {
			scfFlowRecordPage.setValues(null);
		} else {
			scfFlowRecordPage.setValues(scfFlowRecordDao.selectPageByExample(query));
		}

		scfFlowRecordPage.setPagenation(new Pagenation(query.getPageNo(), query.getPageSize(), query.getItemCount()));
		return scfFlowRecordPage;
	}

}
