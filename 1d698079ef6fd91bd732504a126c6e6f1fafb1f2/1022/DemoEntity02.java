package com.hlj.data.bean.demo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/*

 {
	 "TVolumn": 100,
	 "isTmail": true,
	 "name": "HealerJean"
 }

 */
@Data
@Accessors(chain = true)
@ApiModel(value = "demo实体类")
public class DemoEntity02 {


	private String Name ;      // lombok  setName     getName

	private boolean IsTmail;   // lombok  setIsTmail  isIsTmail

	private Long TVolumn;      // lombok  setTVolumn  getTVolumn


}
