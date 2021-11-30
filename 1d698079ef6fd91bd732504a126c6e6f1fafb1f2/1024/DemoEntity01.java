package com.hlj.data.bean.demo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/*

 {
         "TVolumn": 100,
         "name": "HealerJean",
         "tmail": true
  }

*/
@Data
@Accessors(chain = true)
@ApiModel(value = "demo实体类")
public class DemoEntity01 {


	private String name;       //lombok   setName

	private boolean isTmail;   // lombok  setTmail

	private String isString;   // lombok  setTmail

	private Long tVolumn ;     // lombok  setTVolumn


}
