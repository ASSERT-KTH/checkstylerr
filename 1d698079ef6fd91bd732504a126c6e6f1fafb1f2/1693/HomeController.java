package com.hlj.springboot.dome.common.moudle.controller;


import com.hlj.springboot.dome.common.data.ResponseBean;
import com.hlj.springboot.dome.common.moudle.service.DemoEntityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  上午10:22.
 */
@Controller
@RequestMapping(value = "test")
public class HomeController {

    public static int i = 1;


    @Resource
    public DemoEntityService demoEntityService ;

    @GetMapping("noHaveSaveButSaveSuccess")
    @ResponseBody
    public   ResponseBean testNoHaveSaveButSaveSuccess(){
        try {
            demoEntityService.testNoHaveSaveButSaveSuccess();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

    @GetMapping("persist")
    @ResponseBody
    public   ResponseBean persist(){
        try {
            demoEntityService.persist();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

    @GetMapping("persistHaveId")
    @ResponseBody
    public   ResponseBean persistHaveId(){
        try {
            demoEntityService.persistHaveId();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }



    @GetMapping("merge")
    @ResponseBody
    public   ResponseBean merge(){
        try {
            demoEntityService.merge();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }
    @GetMapping("mergeHaveId")
    @ResponseBody
    public   ResponseBean mergeHaveId(){
        try {
            demoEntityService.mergeHaveId();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }




    @GetMapping("remove")
    @ResponseBody
    public   ResponseBean remove(){
        try {
            demoEntityService.remove();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @GetMapping("refresh")
    @ResponseBody
    public   ResponseBean refresh(){
        try {
            demoEntityService.refresh();
            return  ResponseBean.buildSuccess();
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }



}
