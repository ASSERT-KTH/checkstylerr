package com.hlj.mybatisxml.utility;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
/**
 * 
 * @author hlj
 * @date 2017年9月2日
 * @param <T>
 */
public interface BaseMapper<T> extends Mapper<T>,MySqlMapper<T> {

}
