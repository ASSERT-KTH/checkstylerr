package com.hlj.mybatisxml.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hlj.mybatisxml.entity.baseset.BasesetUser;
import com.hlj.mybatisxml.mapper.baseset.BasesetUserMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/26  上午11:59.
 */
@RestController
public class PageHelperController {

    @Resource
    private BasesetUserMapper basesetUserMapper;

    @RequestMapping(value = "page")
    public PageInfo<BasesetUser> queryAll(@RequestParam(value = "pageNum", required = false, defaultValue="1") Integer pageNum,
                                          @RequestParam(value = "pageSize", required = false, defaultValue="10") Integer pageSize) {
        //传入第几页和大小
        PageHelper.startPage(pageNum, pageSize);
        List<BasesetUser> list = basesetUserMapper.findMyall();
        PageInfo<BasesetUser> pageInfo = new PageInfo<>(list);
        return  pageInfo;
    }

}
