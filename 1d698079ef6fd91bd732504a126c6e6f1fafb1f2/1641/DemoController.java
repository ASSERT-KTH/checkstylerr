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
@RequestMapping("demo")
public class DemoController {


    @Autowired
    private DemoEntityService  demoEntityService;


    @ApiOperation(value = "添加demo实体",
                  notes = "添加demo实体",
                  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE,
                  response = DemoEntity.class)
    @GetMapping(value = "addDemoEntity",produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseBean insert(DemoEntity demoEntity){
        try {
            return  ResponseBean.buildSuccess(demoEntityService.addDemoEntity(demoEntity));
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @ApiOperation(notes = "根据id查找Demo实体类",
            value = "根据id查找Demo实体类",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "demo主键",required = true,paramType = "query",dataType = "long"),
    })
    @GetMapping("findById")
    @ResponseBody
    public ResponseBean  findById(Long id){
        try {
            return ResponseBean.buildSuccess(demoEntityService.findById(id));
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }
        catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @ApiOperation(notes = "所有Demo实体类",
            value = "所有Demo实体类",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @GetMapping("findAll")
    @ResponseBody
    public ResponseBean  findAll(){
        try {
            return ResponseBean.buildSuccess(demoEntityService.findAll());
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

}
