package com.hlj.sso.server.rest.Repository;

import com.hlj.sso.server.rest.bean.SysUserRestSalt;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRestSaltRepository extends CrudRepository<SysUserRestSalt,Long>{

    SysUserRestSalt findByEmail(String email);

}
