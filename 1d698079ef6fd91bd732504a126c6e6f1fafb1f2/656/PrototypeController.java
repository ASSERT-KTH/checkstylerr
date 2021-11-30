package com.hlj.moudle.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/14  下午3:53.
 * 类描述：
 */
@Scope("prototype")
@RestController
@RequestMapping("scope")
public class PrototypeController {

    public static int static_n = 1 ;
    public  int no_static_n  = 1 ;

    //http://localhost:8080/scope/prototype
    @ResponseBody
    @GetMapping("prototype")
    public String getN(){
        ++ static_n ;
        ++ no_static_n ;
        return "静态："+static_n +"+非静态"+no_static_n ;
    }

    //静态：2+非静态2
    //静态：3+非静态2
    //静态：4+非静态2
    //静态：5+非静态2
    //静态：6+非静态2
    //静态：7+非静态2
    //静态：8+非静态2

}
