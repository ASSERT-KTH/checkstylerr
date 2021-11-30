package com.hlj.dao.mybatis.demo;

import com.hlj.entity.db.demo.DemoEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 作者 ：HealerJean
 * 日期 ：2018/11/14  下午12:54.
 * 类描述：
 */
public interface SonMapper extends FatherMapper{

    /**
     * 覆盖父类的方法，这里的id为4
     * @return
     */
    DemoEntity extendMethod();

    DemoEntity findById3();

}
