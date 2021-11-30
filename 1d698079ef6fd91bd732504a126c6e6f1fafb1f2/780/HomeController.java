package com.hlj.websocket.WebSocket.viewController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/28  下午7:18.
 */
@Controller
public class HomeController {

    @GetMapping("ws")
    public String ws(){
        return "ws";
    }

    @GetMapping("login")
    public String login(){
        return "login";
    }

    @GetMapping("chat")
    public String chat() {
        return "chat";
    }
}
