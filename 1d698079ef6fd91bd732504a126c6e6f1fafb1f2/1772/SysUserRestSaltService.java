package com.hlj.sso.server.rest.service;

import com.hlj.sso.server.rest.BeanData.SysUserRestSaltData;
import com.hlj.sso.server.rest.bean.SysUserRestSalt;

public interface SysUserRestSaltService {

    SysUserRestSaltData findByEmail(String email);

}
