package com.hlj.graylog.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

    @Slf4j
    @RestController
    public class HomeController {

        @GetMapping("log")
        public String log(){

            log.error("error");
            log.warn("warn");
            log.info("info");
            log.debug("debug");
            int i = 1/0;
            return "success";
        }


}
