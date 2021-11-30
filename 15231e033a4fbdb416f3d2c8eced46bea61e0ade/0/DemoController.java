package com.healerjean.proj.controller;

import com.healerjean.proj.common.ValidateGroup;
import com.healerjean.proj.annotation.InterfaceName;
import com.healerjean.proj.service.DemoEntityService;
import com.healerjean.proj.common.constant.CommonConstants;
import com.healerjean.proj.dto.Demo.DemoDTO;
import com.healerjean.proj.common.ResponseBean;
import com.healerjean.proj.common.enums.ResponseEnum;
import com.healerjean.proj.common.exception.BusinessException;
import com.healerjean.proj.util.json.JsonUtils;
import com.healerjean.proj.util.validate.ValidateUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName DemoController
 * @date 2019/6/13  20:01.
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
@Api(description = "demo控制器")
@Controller
@RequestMapping("hlj")
@Slf4j
public class DemoController {

    @Autowired
    private DemoEntityService demoEntityService;


    @InterfaceName("demo控制器--------demo实体")
    @ApiOperation(value = "demo实体",
            notes = "demo实体",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            response = DemoDTO.class)
    @GetMapping(value = "demo/get", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseBean get(DemoDTO demoDTO) {
        String validate = ValidateUtils.validate(demoDTO, ValidateGroup.HealerJean.class);
        if (!validate.equals(CommonConstants.COMMON_SUCCESS)) {
            throw new BusinessException(ResponseEnum.参数错误, validate);
        }
        return ResponseBean.buildSuccess(demoEntityService.getMmethod(demoDTO));
    }


    @ApiOperation(value = "demo实体",
            notes = "demo实体",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            response = DemoDTO.class)
    @PostMapping(value = "demo/post", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public DemoDTO post(@RequestBody String message) {
        DemoDTO demoDTO = JsonUtils.toObject(message, DemoDTO.class);
        String validate = ValidateUtils.validate(demoDTO, ValidateGroup.HealerJean.class);
        if (!validate.equals(CommonConstants.COMMON_SUCCESS)) {
            throw new BusinessException(ResponseEnum.参数错误, validate);
        }
        return demoEntityService.getMmethod(demoDTO);
    }


    public static void main(String[] args) {
        try {
            // 模拟空指针异常
            //Integer nullInt = Integer.valueOf(null);
            int[] array = {1, 2, 3, 4, 5};
            int outBoundInt = array[5];
        } catch (Exception e) {
            // 使用逗号分隔，调用两个参数的error方法
            log.error("使用 , 号 使第二个参数作为Throwable : ", e);
            // 尝试使用分隔符,第二个参数为Throwable,会发现分隔符没有起作用，第二个参数的不同据，调用不同的重载方法
            log.error("第二个参数为Throwable，使用分隔符打印 {} : ", e);
            log.error("第二个参数为Object，使用分隔符打印 {} ", 123, e);

        }
    }


}
