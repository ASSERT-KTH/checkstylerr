package com.hlj.many.datasourse.controller;


import com.hlj.many.datasourse.dataresource.dao.data.entry.PersonOne;
import com.hlj.many.datasourse.dataresource.dao.data.entry.PersonTwo;
import com.hlj.many.datasourse.dataresource.service.MainService;
import com.hlj.many.datasourse.dataresource.service.OneService;
import com.hlj.many.datasourse.dataresource.service.TwoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  上午10:22.
 */
@RestController
public class HomeController {


    @Resource
    private MainService mainService;

    @Resource
    private OneService oneService;

    @Resource
    private TwoService twoService;

    @GetMapping("default")
    public PersonOne saveDefault(PersonOne personOne){
      return   mainService.save(personOne);
    }

    @GetMapping("one")
    public PersonOne savePersonOne(PersonOne personOne){
        return   oneService.save(personOne);
    }

    @GetMapping("oneFindById")
    public PersonOne findByIdOne(Long id){
        return   oneService.findById(id);
    }

    @GetMapping("two")
    public PersonTwo savepersonTwo(PersonTwo personTwo){
        return   twoService.save(personTwo);
    }

    @GetMapping("twoFindById")
    public PersonTwo findByIdTwo(Long id){
        return   twoService.findById(id);
    }

    @GetMapping("twoFindALL")
    public List<PersonTwo> findByIdTwo(){
        return   twoService.findALL();
    }

}
