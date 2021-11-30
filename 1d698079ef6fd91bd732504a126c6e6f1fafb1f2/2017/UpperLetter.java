package com.hlj.inter;

public class UpperLetter implements ChangeLetter {

	private String str;
	/**
	 * 将大写变成小写
	 */
	@Override
	public String change() {
				
		return str.toUpperCase();
		// TODO Auto-generated method stub
		
	}	
	public String getStr() {
		return str;
	}


	public void setStr(String str) {
		this.str = str;
	}




}
