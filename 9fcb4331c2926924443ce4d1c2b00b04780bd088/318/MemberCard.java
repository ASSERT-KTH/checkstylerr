package me.chanjar.weixin.mp.bean.card;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import me.chanjar.weixin.mp.util.json.WxMpGsonBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

@Data
public final class MemberCard implements Serializable {

  /**
   * 会员卡背景图
   */
  @SerializedName("background_pic_url")
  private String backgroundPicUrl;

  /**
   * 基本信息
   */
  @SerializedName("base_info")
  private BaseInfo baseInfo;

  /**
   * 特权说明
   */
  @SerializedName("prerogative")
  private String prerogative;

  /**
   * 自动激活
   */
  @SerializedName("auto_activate")
  private boolean autoActivate;

  /**
   * 是否一键开卡
   */
  @SerializedName("wx_activate")
  private boolean wxActivate;

  /**
   * 显示积分
   */
  @SerializedName("supply_bonus")
  private boolean supplyBonus;

  /**
   * 查看积分外链,设置跳转外链查看积分详情。仅适用于积分无法通过激活接口同步的情况下使用该字段。
   */
  @SerializedName("bonus_url")
  private String bonusUrl;

  /**
   * 支持储值
   */
  @SerializedName("supply_balance")
  private boolean supplyBalance;

  /**
   * 余额外链,仅适用于余额无法通过激活接口同步的情况下使用该字段。
   */
  @SerializedName("balance_url")
  private String balanceUrl;

  /**
   * 自定义会员类目1,会员卡激活后显示
   */
  @SerializedName("custom_field1")
  private CustomField customField1;

  /**
   * 自定义会员类目2
   */
  @SerializedName("custom_field2")
  private CustomField customField2;

  /**
   * 自定义会员类目3
   */
  @SerializedName("custom_field3")
  private CustomField customField3;

  /**
   * 积分清零规则
   */
  @SerializedName("bonus_cleared")
  private String bonusCleared;

  /**
   * 积分规则
   */
  @SerializedName("bonus_rules")
  private String bonusRules;

  /**
   * 储值规则
   */
  @SerializedName("balance_rules")
  private String balanceRules;

  /**
   * 激活会员卡的url
   */
  @SerializedName("activate_url")
  private String activateUrl;

  /**
   * 激活会原卡url对应的小程序user_name，仅可跳转该公众号绑定的小程序
   */
  @SerializedName("activate_app_brand_user_name")
  private String activateAppBrandUserName;

  /**
   * 激活会原卡url对应的小程序path
   */
  @SerializedName("activate_app_brand_pass")
  private String activateAppBrandPass;

  /**
   * 自定义会员信息类目，会员卡激活后显示。
   */
  @SerializedName("custom_cell1")
  private CustomCell1 customCell1;

  /**
   * 积分规则,JSON结构积分规则 。
   */
  @SerializedName("bonus_rule")
  private BonusRule bonusRule;

  /**
   * 折扣,该会员卡享受的折扣优惠,填10就是九折。
   */
  private Integer discount;

  /**
   * 创建优惠券特有的高级字段
   */
  @SerializedName("advanced_info")
  private AdvancedInfo advancedInfo;

  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
  }

  public static MemberCard fromJson(String json) {
    return WxMpGsonBuilder.INSTANCE.create().fromJson(json, MemberCard.class);
  }
}
