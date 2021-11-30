package com.github.binarywang.wxpay.bean.entpay;

import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;
import me.chanjar.weixin.common.annotation.Required;
import me.chanjar.weixin.common.util.ToStringUtils;

/**
 * <pre>
 * 企业付款请求对象.
 * Created by Binary Wang on 2016/10/02.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor
@AllArgsConstructor
@XStreamAlias("xml")
public class EntPayRequest extends BaseWxPayRequest {
  /**
   * <pre>
   * 字段名：公众账号appid.
   * 变量名：mch_appid
   * 是否必填：是
   * 示例值：wx8888888888888888
   * 类型：String
   * 描述：微信分配的公众账号ID（企业号corpid即为此appId）
   * </pre>
   */
  @XStreamAlias("mch_appid")
  private String mchAppid;

  /**
   * <pre>
   * 字段名：商户号.
   * 变量名：mchid
   * 是否必填：是
   * 示例值：1900000109
   * 类型：String(32)
   * 描述：微信支付分配的商户号
   * </pre>
   */
  @XStreamAlias("mchid")
  private String mchId;

  /**
   * <pre>
   * 字段名：设备号.
   * 变量名：device_info
   * 是否必填：否
   * 示例值：13467007045764
   * 类型：String(32)
   * 描述：微信支付分配的终端设备号
   * </pre>
   */
  @XStreamAlias("device_info")
  private String deviceInfo;

  /**
   * <pre>
   * 字段名：商户订单号.
   * 变量名：partner_trade_no
   * 是否必填：是
   * 示例值：10000098201411111234567890
   * 类型：String
   * 描述：商户订单号
   * </pre>
   */
  @Required
  @XStreamAlias("partner_trade_no")
  private String partnerTradeNo;

  /**
   * <pre>
   * 字段名：需保持唯一性 用户openid.
   * 变量名：openid
   * 是否必填：是
   * 示例值：oxTWIuGaIt6gTKsQRLau2M0yL16E
   * 类型：String
   * 描述：商户appid下，某用户的openid
   * </pre>
   */
  @Required
  @XStreamAlias("openid")
  private String openid;

  /**
   * <pre>
   * 字段名：校验用户姓名选项.
   * 变量名：check_name
   * 是否必填：是
   * 示例值：OPTION_CHECK
   * 类型：String
   * 描述：NO_CHECK：不校验真实姓名 
   * FORCE_CHECK：强校验真实姓名（未实名认证的用户会校验失败，无法转账） 
   * OPTION_CHECK：针对已实名认证的用户才校验真实姓名（未实名认证用户不校验，可以转账成功）
   * </pre>
   */
  @Required
  @XStreamAlias("check_name")
  private String checkName;

  /**
   * <pre>
   * 字段名：收款用户姓名.
   * 变量名：re_user_name
   * 是否必填：可选
   * 示例值：马花花
   * 类型：String
   * 描述：收款用户真实姓名。
   * 如果check_name设置为FORCE_CHECK或OPTION_CHECK，  则必填用户真实姓名
   * </pre>
   */
  @XStreamAlias("re_user_name")
  private String reUserName;

  /**
   * <pre>
   * 字段名：金额.
   * 变量名：amount
   * 是否必填：是
   * 示例值：10099
   * 类型：int
   * 描述：企业付款金额， 单位为分
   * </pre>
   */
  @Required
  @XStreamAlias("amount")
  private Integer amount;

  /**
   * <pre>
   * 字段名：企业付款描述信息.
   * 变量名：desc
   * 是否必填：是
   * 示例值：理赔
   * 类型：String
   * 描述：企业付款操作说明信息。必填。
   * </pre>
   */
  @Required
  @XStreamAlias("desc")
  private String description;

  /**
   * <pre>
   * 字段名：Ip地址.
   * 变量名：spbill_create_ip
   * 是否必填：是
   * 示例值：192.168.0.1
   * 类型：String(32)
   * 描述：调用接口的机器Ip地址
   * </pre>
   */
  @Required
  @XStreamAlias("spbill_create_ip")
  private String spbillCreateIp;

  @Override
  protected void checkConstraints() {

  }

  @Override
  public String getAppid() {
    return this.mchAppid;
  }

  @Override
  public void setAppid(String appid) {
    this.mchAppid = appid;
  }

  @Override
  public String getMchId() {
    return this.mchId;
  }

  @Override
  public void setMchId(String mchId) {
    this.mchId = mchId;
  }

  @Override
  public String toString() {
    return ToStringUtils.toSimpleString(this);
  }

  @Override
  protected boolean ignoreSignType() {
    return true;
  }
}
