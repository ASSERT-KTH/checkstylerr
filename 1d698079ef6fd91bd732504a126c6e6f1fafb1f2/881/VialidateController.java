package com.hlj.moudle.validate;


import com.hlj.data.general.ResponseBean;
import com.hlj.vialidate.ValidateUtils;
import com.hlj.vialidate.data.JavaBean;
import com.hlj.vialidate.group.ValidateGroup;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("hlj")
@Slf4j
public class VialidateController {


    @ApiOperation(value = "Post接口",
            notes = "Post接口",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = ResponseBean.class
    )
    @PostMapping(value = "validate", produces = "application/json;charset=utf-8")
    @ResponseBody
    public ResponseBean post(@RequestBody JavaBean JavaBean) {
        String validate = ValidateUtils.validate(JavaBean, ValidateGroup.HealerJean.class);
        if (!"success".equals(validate)) {
            log.info("错误信息：{}", validate);
        }
        return ResponseBean.buildSuccess(validate);
    }


}


