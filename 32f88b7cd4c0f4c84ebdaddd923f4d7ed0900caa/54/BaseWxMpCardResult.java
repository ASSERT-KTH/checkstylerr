package me.chanjar.weixin.mp.bean.card;

import java.io.Serializable;

/**
 * @description 卡券返回结果基础类
 * @author: fanxl
 * @date: 2019/1/22 0022 10:08
 */
public class BaseWxMpCardResult implements Serializable {

  /**
   * 错误码
   */
  private Integer errcode;

  /**
   * 错误信息
   */
  private String errmsg;

  public boolean isSuccess() {
    return 0 == errcode;
  }
}
