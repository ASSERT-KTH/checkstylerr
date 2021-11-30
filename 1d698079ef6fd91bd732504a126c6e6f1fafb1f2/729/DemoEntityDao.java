/*
 * Copyright (C) 2019 xiaomi.com, Inc. All Rights Reserved.
 */package com.hlj.proj.data.dao.mybatis.dao.deme;

import com.hlj.proj.data.dao.mybatis.dao.BaseDao;
import com.hlj.proj.data.pojo.demo.DemoEntity;
import com.hlj.proj.data.pojo.demo.DemoEntityQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @author zhangyujin
 * @ClassName: DemoEntityDao
 * @date 2099/1/1
 * @Description: DemoEntityDao
 */
@Repository("demoEntityDao")
public class DemoEntityDao extends BaseDao {

	public int countByExample(DemoEntityQuery example) {
		return super.getSqlSession().selectOne("DemoEntityMapper.countByExample", example);
	}

	public int deleteByExample(DemoEntityQuery example) {
		return super.getSqlSession().delete("DemoEntityMapper.deleteByExample", example);
	}

	public int deleteByPrimaryKey(long id) {
		return super.getSqlSession().delete("DemoEntityMapper.deleteByPrimaryKey", id);
	}

	public int insert(DemoEntity record) {
		return super.getSqlSession().insert("DemoEntityMapper.insert", record);
	}

	public int insertSelective(DemoEntity record) {
		return super.getSqlSession().insert("DemoEntityMapper.insertSelective", record);
	}
	
	public int batchInsert(List<DemoEntity> list) {
		return super.batchInsert("DemoEntityMapper.insertSelective", list);
	}

	public List<DemoEntity> selectByExample(DemoEntityQuery example) {
		return super.getSqlSession().selectList("DemoEntityMapper.selectByExample", example);
	}

	public List<DemoEntity> selectPageByExample(DemoEntityQuery example) {
		return super.getSqlSession().selectList("DemoEntityMapper.selectPageByExample", example);
	}

	public DemoEntity selectByPrimaryKey(long id) {
		return super.getSqlSession().selectOne("DemoEntityMapper.selectByPrimaryKey", id);
	}

	public int updateByPrimaryKeySelective(DemoEntity record) {
		return super.getSqlSession().update("DemoEntityMapper.updateByPrimaryKeySelective", record);
	}

	public int updateByPrimaryKey(DemoEntity record) {
		return super.getSqlSession().update("DemoEntityMapper.updateByPrimaryKey", record);
	}

}
