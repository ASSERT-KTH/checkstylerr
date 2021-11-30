package com.hlj.proj.data.common.query;

import lombok.Data;

import java.io.Serializable;


/**
 * 分页查询
 * @author YuYue
 *
 */
@Data
public abstract class PagingQuery implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Integer pageNo ;
	private Integer pageSize ;
	
	/**
	 * 数据记录总数
	 */
	private Long itemCount;
	private Integer startRow;
	private Integer endRow;
	
	public PagingQuery(Integer pageNo, Integer pageSize){
		this.pageNo=pageNo;
		this.pageSize=pageSize;
	}


    public void setItemCount(Long itemCount){
    	this.itemCount = itemCount;
    	startRow = (pageNo - 1) * pageSize;
    	endRow = pageSize;
    }

}
