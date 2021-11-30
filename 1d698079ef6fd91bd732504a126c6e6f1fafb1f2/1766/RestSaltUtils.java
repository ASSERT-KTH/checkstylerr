package com.hlj.sso.server.Utils;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;

public class RestSaltUtils {


    /**
     * 产生随机盐
     */
    public String salt(String password) {
        SecureRandomNumberGenerator secureRandomNumberGenerator = new SecureRandomNumberGenerator();
        String salt = secureRandomNumberGenerator.nextBytes().toHex();
        System.out.println("随机盐："+salt);
        //对密码加密后,将加密后的密码和盐存入对象
        password = new Md5Hash(password, salt).toString();
        System.out.println("密码："+password);

        return password;
    }

    public static void main(String[] args) {
        String password ="HealerJean123456";
        RestSaltUtils restSaltUtils = new RestSaltUtils();
        restSaltUtils.salt(password);
    }

}
