package com.hlj.Ehcache.controller;


import com.hlj.Ehcache.service.EhcacheService;
import com.hlj.common.bean.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/21  下午1:58.
 */
@Controller
public class EhCacheController {


    @Autowired
    private EhcacheService ehcacheService;

    @GetMapping("save")
    public @ResponseBody Person save(Long id) {
        Person person = new Person(id,"EhcacheHealerJean","123465");
        return ehcacheService.save(person);
    }

    @GetMapping("findById")
    public @ResponseBody Person findById(Long id) {
        return ehcacheService.findById(id);
    }

    @GetMapping("update")
    public @ResponseBody Person update(Person person) {
        return ehcacheService.update(person);
    }

    @GetMapping("delete")
    public @ResponseBody String delete(Long id) {
        ehcacheService.delete(id);
        return "删除成功";
    }

    @GetMapping("listPerson")
    public@ResponseBody  List<Person> listPerson() {
        return ehcacheService.listPerson();
    }
}
