package com.hlj.springboot.dome.common.moudle.controller;


import com.hlj.springboot.dome.anno.JSON;
import com.hlj.springboot.dome.common.data.ResponseBean;
import com.hlj.springboot.dome.common.entity.DemoEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  上午10:22.
 */
@Controller
public class HomeController {



    @GetMapping("responBean")
    @ResponseBody
    @JSON(type =DemoEntity.class, include = "name")
    public ResponseBean jsonIgnore(){
        try {
            DemoEntity demoEntity = new DemoEntity().setId(1L).setName("healerjean").setBalance(24L);
            return  ResponseBean.buildSuccess(demoEntity);
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

    @GetMapping("responBean/list")
    @ResponseBody
    @JSON(type =DemoEntity.class, filter = "id")
    public ResponseBean list(){
        try {
            List<DemoEntity> demoEntityList  = new ArrayList<>();
            DemoEntity demoEntity = new DemoEntity().setId(1L).setName("healerjean").setBalance(24L);
            DemoEntity demoEntity2 = new DemoEntity().setId(1L).setName("healerjean").setBalance(24L);
            demoEntityList.add(demoEntity);
            demoEntityList.add(demoEntity2);
            return  ResponseBean.buildSuccess(demoEntityList);
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }

    @GetMapping("jsonFilter")
    @ResponseBody
    @JSON(type =DemoEntity.class, filter = "id")
    public DemoEntity jsonFilter(){
        try {
            DemoEntity demoEntity = new DemoEntity().setId(1L).setName("healerjean").setBalance(24L);
            return  demoEntity;
        }catch (Exception e){
            return  null;
        }
    }

    @GetMapping("jsonInclude/list")
    @ResponseBody
    @JSON(type =DemoEntity.class, include = "id")
    public List<DemoEntity> jsonIncludeList(){
        try {
            List<DemoEntity> demoEntityList  = new ArrayList<>();
            DemoEntity demoEntity = new DemoEntity().setId(1L).setName("healerjean").setBalance(24L);
            DemoEntity demoEntity2 = new DemoEntity().setId(1L).setName("healerjean").setBalance(24L);
            demoEntityList.add(demoEntity);
            demoEntityList.add(demoEntity2);
            return  demoEntityList;
        }catch (Exception e){
            return  null;
        }
    }



}
