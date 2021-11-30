package com.asynch.common;

import com.asynch.error.ExceptionHandler;


public final class Tuple extends ExceptionHandler{

	private String url;
	private String pageSource;

	public Tuple() {
		super();		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPageSource() {
		return pageSource;
	}

	public void setPageSource(String pageSource) {
		this.pageSource = pageSource;
	}

}
