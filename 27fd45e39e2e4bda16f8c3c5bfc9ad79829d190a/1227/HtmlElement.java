package com.javacodegeeks.patterns.visitorpattern;

public class HtmlElement extends HtmlTag{

	private String tagName;
	private String startTag; 
	private String endTag;
	private String tagBody;
	
	public HtmlElement(String tagName){
		this.tagName = tagName;
		this.tagBody = "";
		this.startTag = "";
		this.endTag = "";
	}
	
	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public void setStartTag(String tag) {
		this.startTag = tag;
	}
	
	@Override
	public void setEndTag(String tag) {
		this.endTag = tag;
	}
	
	@Override
	public String getStartTag() {
		return startTag;
	}
	
	@Override
	public String getEndTag() {
		return endTag;
	}
	
	@Override
	public void setTagBody(String tagBody){
		this.tagBody = tagBody;
	}
	
	@Override
	public void generateHtml() {
		System.out.println(startTag+""+tagBody+""+endTag);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
