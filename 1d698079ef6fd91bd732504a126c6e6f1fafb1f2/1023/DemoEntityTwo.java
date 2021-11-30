package com.hlj.data.bean.demo;

import io.swagger.annotations.ApiModel;
import net.sf.json.JSONObject;

/*
{
    "TVolumn": 0,
    "healerJean": false,
    "id": 0,
    "name": "",
    "tVolumn": 0,
    "tmail": false
}

*/
@ApiModel(value = "demo实体类")
public class DemoEntityTwo {

	private Long id;

	private String Name ;   // 谁在前面用谁的 set get
	private String name;

	private boolean isTmail; // 谁在前面用谁的 setTmail isTmail
	private boolean IsTmail;

	private Long tVolumn ;  //settVolumn

	private Long TVolumn;   //setTVolumn

	private boolean healerJean ; //boolean类型的时候 setHealerJean   isHealerJean


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
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

	public Long getTVolumn() {
		return TVolumn;
	}

	public void setTVolumn(Long TVolumn) {
		this.TVolumn = TVolumn;
	}


	public boolean isHealerJean() {
		return healerJean;
	}

	public void setHealerJean(boolean healerJean) {
		this.healerJean = healerJean;
	}


	public static void main(String[] args) {
		System.out.println(JSONObject.fromObject(new DemoEntityTwo()).toString());
	}
}
