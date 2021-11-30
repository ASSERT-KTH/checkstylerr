package com.hlj.ddkj.ExceptionHandler.HaveException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionController {

    @GetMapping("exception")
    public String exception(){

        int i = 1/0;
        return "No exception";
    }
}
