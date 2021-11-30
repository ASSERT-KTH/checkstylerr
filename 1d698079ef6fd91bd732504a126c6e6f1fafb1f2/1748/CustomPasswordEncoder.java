package com.hlj.sso.server.EncoderUtils;

import com.hlj.sso.server.Utils.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 自定义密码认证
 *
 * @author chuan.ma
 * @since 2017/6/22
 */
public class CustomPasswordEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPasswordEncoder.class);

    //前台传来的明文密码会进入这里。然后取得加密密码
    @Override
    public String encode(CharSequence password) {
        if (password == null) {
            return null;
        } else {
            return this.encodePassword(password.toString());
        }
    }
    //密码校验，匹配
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String encodedRawPassword = StringUtils.isNotBlank(rawPassword)?this.encode(rawPassword.toString()):null;
        return StringUtils.equals(encodedRawPassword, encodedPassword);
    }

    // 这部分则为通过前台传来的明文密码根据自己的东西返回获取数据库中的加密密码的格式，
    // 用于matchs方法中进行比较
    private String encodePassword(String password) {
        return MD5Util.string2MD5(password);
    }
}