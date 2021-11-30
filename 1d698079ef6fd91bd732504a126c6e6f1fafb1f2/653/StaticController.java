package com.hlj.moudle.staticT;

import com.hlj.moudle.staticT.service.StaticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/14  下午4:08.
 * 类描述：
 */
@RestController
@RequestMapping("static")
public class StaticController {

    @Resource
    private    StaticService staticService ;

    //http://localhost:8080/static/addNostaticN
    @GetMapping("addNostaticN")
    @ResponseBody
    public String addNostaticN(){
        return staticService.addNostaticN();
    }
    //非静态2
    //非静态3
    //非静态4
    //非静态5
    //非静态6
    //非静态7
    //非静态8

    //http://localhost:8080/static/addNostaticN
    @GetMapping("addStaticN")
    @ResponseBody
    public String addStaticN(){
        return staticService.addStaticN();
    }
    //静态2
    //静态3
    //静态4
    //静态5
    //静态6
    //静态7
    //静态8


}



