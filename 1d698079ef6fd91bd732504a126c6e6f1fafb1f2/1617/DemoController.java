package com.hlj.moudle.demo.controller;


import com.hlj.data.general.AppException;
import com.hlj.data.general.ResponseBean;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.demo.service.DemoEntityService;
import com.hlj.utils.ExceptionLogUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
@RequestMapping("mapper")
public class DemoController {


    @Autowired
    private DemoEntityService  demoEntityService;

    @ApiOperation(notes = "mapper继承",
            value = "mapper继承",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @GetMapping("extend")
    @ResponseBody
    public ResponseBean  mapperExtend(){
        try {
            return ResponseBean.buildSuccess(demoEntityService.mapperExtend());
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

}
