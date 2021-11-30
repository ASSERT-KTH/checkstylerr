package com.hlj.moudle.produce;


import com.hlj.data.general.AppException;
import com.hlj.data.general.ResponseBean;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.produce.service.ProcedureService;
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
@Api(description = "存储过程控制器")
@Controller
@RequestMapping("demo")
public class ProcedureController {


    @Autowired
    private ProcedureService procedureService;

    @ApiOperation(notes = "存储过程out结果获取",
            value = "存储过程out结果获取",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "demo主键",required = true,paramType = "query",dataType = "long"),
    })
    @GetMapping("procedureGetOut")
    @ResponseBody
    public ResponseBean  procedureGetOut(Long id){
        try {
            return ResponseBean.buildSuccess(procedureService.procedureGetOut(id));
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }
        catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @ApiOperation(notes = "获取返回结果集",
            value = "获取返回结果集",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",defaultValue = "HealerJean", value = "比如 HealerJean",required = true,paramType = "query",dataType = "long"),
    })
    @GetMapping("procedureGetOneList")
    @ResponseBody
    public ResponseBean  procedureGetOneList(String name){
        try {
            return ResponseBean.buildSuccess(procedureService.procedureGetOneList(name));
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }
        catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @ApiOperation(notes = "获取返回结果集(多个)",
            value = "获取返回结果集(多个)",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oneName",defaultValue = "HealerJean", value = "比如 HealerJean",required = true,paramType = "query",dataType = "long"),
            @ApiImplicitParam(name = "twoName",defaultValue = "Healer", value = "比如 HealerJean",required = true,paramType = "query",dataType = "long"),
    })
    @GetMapping("procedureGetManyList")
    @ResponseBody
    public ResponseBean  procedureGetManyList(String oneName,String twoName){
        try {
            return ResponseBean.buildSuccess(procedureService.procedureGetManyList(oneName,twoName));
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }
        catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }



}
