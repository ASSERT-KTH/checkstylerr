package com.hlj.druid.controller;

import com.hlj.druid.common.Format.ResponseBean;
import com.hlj.druid.common.bean.Person;
import com.hlj.druid.common.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  上午10:22.
 */
@Controller
public class HomeController {

    @Autowired
    private PersonRepository personRepository;

    @GetMapping("insert")
    public @ResponseBody ResponseBean insert(Person person){
        try {
            return  ResponseBean.buildSuccess(personRepository.save(person));
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }


    @GetMapping("findById")
    @ResponseBody
    public ResponseBean  findById(Long id){
        try {
            return ResponseBean.buildSuccess(personRepository.findById(id));
        }catch (Exception e){
            return  ResponseBean.buildFailure(e.getMessage());
        }
    }
}
