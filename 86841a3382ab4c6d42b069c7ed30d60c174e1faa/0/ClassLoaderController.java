package com.hlj.moudle.demo.controller;

import com.hlj.data.general.AppException;
import com.hlj.data.general.ResponseBean;
import com.hlj.moudle.Jvm03类加载器.Jvm02Test;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.Set;

/**
 * @author HealerJean
 * @ClassName ClassLoaderController
 * @date 2020/1/10  16:18.
 * @Description
 */
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "访问正常"),
        @ApiResponse(code = 301, message = "逻辑错误"),
        @ApiResponse(code = 500, message = "系统错误"),
        @ApiResponse(code = 401, message = "未认证"),
        @ApiResponse(code = 403, message = "禁止访问"),
        @ApiResponse(code = 404, message = "url错误")
})
@Api(description = "ClassLoader控制器")
@Controller
@RequestMapping("classLoader")
@Slf4j
public class ClassLoaderController {


    @ApiOperation(value = "Get接口", notes = "Get接口",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseBean.class)
    @GetMapping("")
    @ResponseBody
    public ResponseBean get() {
        try {
            Set<ClassLoader> set = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    ClassLoader classLoader = Jvm02Test.class.getClassLoader();
                    set.add(classLoader);
                }).start();
            }
            Thread.sleep(6000L);
            System.out.println("----------------------");
            System.out.println(set);
            return ResponseBean.buildSuccess(set);
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            return ResponseBean.buildFailure(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

}
