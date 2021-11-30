package com.hlj.mybatisxml.controller;

import com.hlj.mybatisxml.entity.baseset.BasesetUser;
import com.hlj.mybatisxml.entity.baseset.Country;
import com.hlj.mybatisxml.mapper.baseset.BasesetUserMapper;
import com.hlj.mybatisxml.mapper.baseset.CountryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/26  上午10:31.
 */
@RestController
public class HomeController {

    @Resource
    private BasesetUserMapper basesetUserMapper;

    @Resource
    private CountryMapper countryMapper ;
    @GetMapping("")
    public String home(){
        return "Hello";

    }

    @GetMapping("insert")
    public BasesetUser insert(){

        BasesetUser user = new BasesetUser();
        user.setUsername("HealerJean");
        user.setPassword("213456");
        user.setEnable(1);

        try {
            basesetUserMapper.insert(user);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BasesetUser();
    }

    @GetMapping("findById")
      public BasesetUser findById(int id){
       return     basesetUserMapper.selectByPrimaryKey(id);
    }


    @GetMapping("myMethod")
    public List<BasesetUser> getMyALL(){
      return   basesetUserMapper.findMyall();
    }


    @GetMapping("country")
    public Country country(){
        Country country = new Country();
        country.setCountryname("中国");
        country.setCountrycode("ZH");
        countryMapper.insert(country);

        return country;
    }

}
