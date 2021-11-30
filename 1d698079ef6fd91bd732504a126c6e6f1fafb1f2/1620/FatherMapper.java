package com.hlj.dao.mybatis.demo;

import com.hlj.entity.db.demo.DemoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作者 ：HealerJean
 * 日期 ：2018/11/14  下午12:54.
 * 类描述：
 */
public interface FatherMapper {

    DemoEntity findById1();

    DemoEntity extendMethod();

}
