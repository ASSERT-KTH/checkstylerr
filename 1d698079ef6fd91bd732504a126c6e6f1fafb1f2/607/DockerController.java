package com.hlj.spring.boot.docker.controller;

import com.hlj.spring.boot.docker.utils.DateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

@Controller
public class DockerController {


    Logger logger = LoggerFactory.getLogger(DockerController.class);

    @RequestMapping("/")
    public String index() {
        return DateHelper.convertDate2String(new Date(),DateHelper.YYYY_MM_DD_HH_MM_SS);
    }


    @GetMapping("date")
    @ResponseBody
    public String date() {
        logger.info(new Date().toString());
        return DateHelper.convertDate2String(new Date(),DateHelper.YYYY_MM_DD_HH_MM_SS);
    }

}



