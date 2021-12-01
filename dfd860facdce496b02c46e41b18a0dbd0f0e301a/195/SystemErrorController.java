package com.java110.front.controller;

import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.util.StringUtil;
import com.java110.core.base.controller.BaseController;
import com.java110.service.api.BusinessApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 错误页面
 * Created by wuxw on 2018/5/2.
 */
@Controller
public class SystemErrorController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(BusinessApi.class);


    @RequestMapping(path = "/system/error")
    public String error(Model model, HttpServletRequest request) {
        String code = request.getParameter("code");
        String msg = request.getParameter("msg");
        if(StringUtil.isNullOrNone(code) || StringUtil.isNullOrNone(msg)){
            code = ResponseConstant.RESULT_CODE_INNER_ERROR;
            msg = "系统内部异常";
        }
        model.addAttribute("code",code);
        model.addAttribute("msg",msg);
        //3.0 查询各个系统调用量
        return "error";
    }
}
