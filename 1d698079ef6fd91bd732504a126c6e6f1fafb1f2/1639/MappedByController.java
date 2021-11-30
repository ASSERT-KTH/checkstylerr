package com.hlj.moudle.mappedBy.controller;

import com.hlj.data.general.AppException;
import com.hlj.data.general.ResponseBean;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.mappedBy.Service.MappedByService;
import com.hlj.utils.ExceptionLogUtils;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/4  下午2:06.
 * 类描述：
 */
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "访问正常"),
        @ApiResponse(code = 301, message = "逻辑错误"),
        @ApiResponse(code = 500, message = "系统错误"),
        @ApiResponse(code = 401, message = "未认证"),
        @ApiResponse(code = 403, message = "禁止访问"),
        @ApiResponse(code = 404, message = "url错误")
})
@Api(description = "MappedBy控制器")
@Controller
@RequestMapping("mappedBy")
public class MappedByController {

    @Resource
    private MappedByService mappedByService;

    @ApiOperation(value = "没有设置mappedBy属性,默认是都由自身维护关联关系",
            notes = "没有设置mappedBy属性,默认是都由自身维护关联关系",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @GetMapping(value = "noMappedBy",produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseBean noMappedBy(){
        try {
            mappedByService.noMappedBy();
            return  ResponseBean.buildSuccess();
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }






    @ApiOperation(value = "懒加载和急加载",
            notes = "懒加载和急加载",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "departmentId",value = "departmentId " ,paramType = "query",dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "employeeId",value = "employeeId " ,paramType = "query",dataTypeClass = Integer.class),
    })
    @GetMapping(value = "fetch",produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseBean fetch(Integer departmentId, Integer employeeId){
        try {

            mappedByService.fetch(departmentId,employeeId);

            return  ResponseBean.buildSuccess();
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }
}
