package me.chanjar.weixin.mp.bean.result;

import java.io.Serializable;

import lombok.Data;
import me.chanjar.weixin.mp.bean.WxMpCard;
import me.chanjar.weixin.mp.util.json.WxMpGsonBuilder;

/**
 * 卡券查询Code，核销Code接口返回结果
 *
 * @author YuJian
 * @version 15/11/11
 */
@Data
public class WxMpCardResult implements Serializable {
  private static final long serialVersionUID = -7950878428289035637L;

  private String errorCode;

  private String errorMsg;

  private String openId;

  private WxMpCard card;

  private String userCardStatus;

  private Boolean canConsume;

  @Override
  public String toString() {
    return WxMpGsonBuilder.create().toJson(this);
  }

}
