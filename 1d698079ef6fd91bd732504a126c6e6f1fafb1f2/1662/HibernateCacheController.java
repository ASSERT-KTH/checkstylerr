package com.hlj.moudle.cache;


import com.hlj.data.general.AppException;
import com.hlj.data.general.ResponseBean;
import com.hlj.entity.db.demo.DemoEntity;
import com.hlj.moudle.cache.service.HibernateCacheService;
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
@Api(description = "hibernate/cache")
@Controller
@RequestMapping("hibernate/cache")
public class HibernateCacheController {


    @Autowired
    private HibernateCacheService hibernateCacheService;


    @ApiOperation(value = "一级缓存",
                  notes = "一级缓存",
                  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE,
                  response = DemoEntity.class)
    @GetMapping(value = "one_cache",produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseBean insert(Long  id){
        try {
            hibernateCacheService.oneCache(id) ;
            return  ResponseBean.buildSuccess();
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }



    @ApiOperation(value = "更新缓存",
            notes = "更新缓存",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @GetMapping(value = "updateOneCache",produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseBean updateOneCache(Long  id){
        try {
            hibernateCacheService.updateOneCache(id) ;
            return  ResponseBean.buildSuccess();
        }catch (AppException e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getCode(),e.getMessage());
        }catch (Exception e){
            ExceptionLogUtils.log(e,this.getClass() );
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @ApiOperation(value = "二级缓存",
            notes = "二级缓存",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = DemoEntity.class)
    @GetMapping(value = "two_cache",produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseBean two_cache(Long  id){
        try {
            hibernateCacheService.twoCache(id) ;
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
