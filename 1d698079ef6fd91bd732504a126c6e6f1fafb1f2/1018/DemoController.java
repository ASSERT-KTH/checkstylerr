package com.hlj.moudle.demo.controller;


import com.hlj.data.bean.demo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  上午10:22.
 */
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "访问正常"),
        @ApiResponse(code = 301, message = "逻辑错误"),
        @ApiResponse(code = 500, message = "系统错误"),
        @ApiResponse(code = 401, message = "未认证"),
        @ApiResponse(code = 403, message = "禁止访问"),
        @ApiResponse(code = 404, message = "url错误")
})
@Api(description = "demo控制器")
@Controller
@RequestMapping("demo")
@Slf4j
public class DemoController {


    /*
     lombok
     private String name;       // lombok  setName
     private boolean isTmail;   // lombok  setTmail
     private Long tVolumn ;     // lombok  setTVolumn

         {
         "TVolumn": 100,
         "id": 1,
         "name": "HealerJean",
         "tmail": true
         }

     */
    @ResponseBody
    @GetMapping(value = "1")
    public String demo01(){
        String json ;
        DemoEntity01 demoEntity =    new DemoEntity01() //lombok
                .setName("HealerJean")    //  lombok   name
                .setTmail(true)           //  lombok  isTmail
                .setTVolumn(100L)         //  lombok   tVolumn
                ;

        demoEntity.getName() ;
        demoEntity.getTVolumn();
        demoEntity.isTmail();

        json = JSONObject.fromObject(demoEntity).toString();
        log.info(json);
        return json ;
    }


    /*
        {
            "TVolumn": 100,
            "isTmail": true,
            "name": "HealerJean"
        }
        private String Name ;      // lombok  setName
        private boolean IsTmail;   // lombok  setIsTmail
        private Long TVolumn;      // lombok  setTVolumn
     */


    @ResponseBody
    @GetMapping(value = "2")
    public String demo02(){
        String json ;
        DemoEntity02 demoEntity =    new DemoEntity02() //lombok
                .setName("HealerJean")  //  lombok    name
                .setIsTmail(true)       //  lombok    IsTmail
                .setTVolumn(100L)       //  lombok    TVolumn
                ;

        demoEntity.getName() ;
        demoEntity.getTVolumn();
        demoEntity.isIsTmail();

         json = JSONObject.fromObject(demoEntity).toString();
        log.info(json);
        return  json ;
    }


    /*
        JavaBean

	private String name;       //    setName     getName
	private boolean isTmail;   //    setTmail     isTmail
	private Long tVolumn ;     //    settVolumn  gettVolumn

    {
        "name": "HealerJean",
        "tVolumn": 100,
        "tmail": true
    }

     */

    @ResponseBody
    @GetMapping(value = "3")
    public String demo03(){
        String json ;
        DemoEntity03 demoEntity =    new DemoEntity03(); //JavaBean

        demoEntity.setName("HealerJean");
        demoEntity.setTmail(true);
        demoEntity.settVolumn(100L);

        demoEntity.getName() ;
        demoEntity.isTmail();       // JavaBean isTmail
        demoEntity.gettVolumn();    // JavaBean  tVolumn

        json = JSONObject.fromObject(demoEntity).toString();
        log.info(json);
        return  json ;
    }


    /*
      JavaBean

        private String Name;       //   setName      getName
        private boolean IsTmail;   //   setTmail     isTmail
        private Long TVolumn ;     //   setTVolumn   getTVolumn

        {
            "TVolumn": 100,
            "name": "HealerJean",
            "tmail": true
        }

     */
    @ResponseBody
    @GetMapping(value = "4")
    public String demo04(){
        String json ;
        DemoEntity04 demoEntity =    new DemoEntity04() ; //JavaBean

        demoEntity.setName("HealerJean");
        demoEntity.setTmail(true);
        demoEntity.setTVolumn(100L);

        demoEntity.getName() ;
        demoEntity.isTmail();       // JavaBean IsTmail
        demoEntity.getTVolumn();    // JavaBean  TVolumn

        json = JSONObject.fromObject(demoEntity).toString();
        log.info(json);
        return  json ;
    }



}
