package com.hlj.data.bean.demo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

/*
	{
		"TVolumn": 100,
		"healerJean": true,
		"id": 0,
		"isTmail": true,
		"name": "HealerJean",
		"tmail": true
	}
*/
@Data
@Accessors(chain = true)
@ApiModel(value = "demo实体类")
public class DemoEntityOne {

	private Long id;
	private String name;       //lombok   setName
	private String Name ;      // lombok  setName

	private boolean isTmail;   // lombok  setTmail     isTmail

	private boolean IsTmail;   // lombok  setIsTmail   isIsTmail


	private Long tVolumn ;     //  这两个在lombok中显示的是一样的，谁在前使用谁  setTVolumn  getTVolumn
	private Long TVolumn;      //


	private boolean healerJean ; //boolean类型的时候 setHealerJean   isHealerJean


	public static void main(String[] args) {

		//lombok 针对boolean类型的 使用is
		DemoEntityOne demoEntityOne = 	new DemoEntityOne().setIsTmail(true).setTmail(true).setTVolumn(100L).setName("HealerJean")
				     .setHealerJean(true) ;

		demoEntityOne.isHealerJean() ;
		demoEntityOne.isIsTmail() ;
		demoEntityOne.isTmail();

		demoEntityOne.getTVolumn();

		System.out.println((JSONObject.fromObject(demoEntityOne).toString()));

	}




}
