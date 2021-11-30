package com.hlj.data.bean.demo;

import io.swagger.annotations.ApiModel;

/*
{
		"TVolumn": 100,
		"name": "HealerJean",
		"tmail": true
}
 */
@ApiModel(value = "demo实体类")
public class DemoEntity04 {


	private String Name;       //   setName      getName
	private boolean IsTmail;   //   setTmail     isTmail
	private Long TVolumn ;     //   setTVolumn   getTVolumn



	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public boolean isTmail() {
		return IsTmail;
	}

	public void setTmail(boolean tmail) {
		IsTmail = tmail;
	}

	public Long getTVolumn() {
		return TVolumn;
	}

	public void setTVolumn(Long TVolumn) {
		this.TVolumn = TVolumn;
	}
}
