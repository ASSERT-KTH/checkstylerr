package com.hlj.sso.server.rest.service.impl;

import com.hlj.sso.server.rest.BeanData.SysUserRestSaltData;
import com.hlj.sso.server.rest.Repository.SysUserRestSaltRepository;
import com.hlj.sso.server.rest.bean.SysUserRestSalt;
import com.hlj.sso.server.rest.service.SysUserRestSaltService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserRestSaltServiceImpl implements SysUserRestSaltService{

    @Autowired
    private SysUserRestSaltRepository sysUserRestSaltRepository;

    @Override
    public SysUserRestSaltData findByEmail(String email) {
        SysUserRestSalt sysUserRestSalt =sysUserRestSaltRepository.findByEmail(email);

        SysUserRestSaltData sysUserRestSaltData = new SysUserRestSaltData();
       if(sysUserRestSalt!=null) {
           sysUserRestSaltData.setUsername(sysUserRestSalt.getUsername());
           sysUserRestSaltData.setEmail(sysUserRestSalt.getEmail());
           sysUserRestSaltData.setId(sysUserRestSalt.getId());
           sysUserRestSaltData.setLocked(sysUserRestSalt.getLocked());
           sysUserRestSaltData.setDisable(sysUserRestSalt.getDisable());
           sysUserRestSaltData.setExpired(sysUserRestSalt.getExpired());
           sysUserRestSaltData.setPassword(sysUserRestSalt.getPassword());
           sysUserRestSaltData.setSalt(sysUserRestSalt.getSalt());
           //下面这两个用到，可有可没有
           sysUserRestSaltData.setAddress(sysUserRestSalt.getAddress());
           sysUserRestSaltData.setAge(sysUserRestSalt.getAge());

       }
        return sysUserRestSaltData;
    }
}
