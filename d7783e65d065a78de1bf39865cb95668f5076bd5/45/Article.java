package com.asynch.common;

import com.asynch.error.ExceptionHandler;

public final class Article extends ExceptionHandler{

	private String rawHtml, cleanContent;
	private String title, url;

	public String getRawHtml() {
		return rawHtml;
	}

	public void setRawHtml(String rawHtml) {
		this.rawHtml = rawHtml;
	}

	public String getCleanContent() {
		return cleanContent;
	}

	public void setCleanContent(String cleanContent) {
		this.cleanContent = cleanContent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {
		return new StringBuilder("Url : ")
		.append(url).append("\n").append("Title : ")
		.append(title).toString();
	}

}
