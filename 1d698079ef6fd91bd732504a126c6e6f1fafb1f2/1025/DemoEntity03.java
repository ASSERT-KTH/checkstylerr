package com.hlj.data.bean.demo;

import io.swagger.annotations.ApiModel;

/*
 {
	 "name": "HealerJean",
	 "tVolumn": 100,
	 "tmail": true
 }


 */
@ApiModel(value = "demo实体类")
public class DemoEntity03 {


	private String name;       // JavaBean   setName     getName
	private boolean isTmail;   // JavaBean   setTmail     isTmail
	private Long tVolumn ;     // JavaBean   settVolumn  gettVolumn


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isTmail() {
		return isTmail;
	}

	public void setTmail(boolean tmail) {
		isTmail = tmail;
	}

	public Long gettVolumn() {
		return tVolumn;
	}

	public void settVolumn(Long tVolumn) {
		this.tVolumn = tVolumn;
	}
}
