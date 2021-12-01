package com.github.binarywang.wxpay.bean.request;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.util.SignUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.util.BeanUtils;
import me.chanjar.weixin.common.util.json.WxGsonBuilder;
import me.chanjar.weixin.common.util.xml.XStreamInitializer;

import static com.github.binarywang.wxpay.constant.WxPayConstants.SignType.ALL_SIGN_TYPES;

/**
 * <pre>
 *  微信支付请求对象共用的参数存放类
 * Created by Binary Wang on 2016-10-24.
 * </pre>
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Data
public abstract class BaseWxPayRequest implements Serializable {
  private static final long serialVersionUID = -4766915659779847060L;

  /**
   * <pre>
   * 字段名：公众账号ID.
   * 变量名：appid
   * 是否必填：是
   * 类型：String(32)
   * 示例值：wxd678efh567hg6787
   * 描述：微信分配的公众账号ID（企业号corpid即为此appId）
   * </pre>
   */
  @XStreamAlias("appid")
  protected String appid;
  /**
   * <pre>
   * 字段名：商户号.
   * 变量名：mch_id
   * 是否必填：是
   * 类型：String(32)
   * 示例值：1230000109
   * 描述：微信支付分配的商户号
   * </pre>
   */
  @XStreamAlias("mch_id")
  protected String mchId;
  /**
   * <pre>
   * 字段名：服务商模式下的子商户公众账号ID.
   * 变量名：sub_appid
   * 是否必填：是
   * 类型：String(32)
   * 示例值：wxd678efh567hg6787
   * 描述：微信分配的子商户公众账号ID
   * </pre>
   */
  @XStreamAlias("sub_appid")
  protected String subAppId;
  /**
   * <pre>
   * 字段名：服务商模式下的子商户号.
   * 变量名：sub_mch_id
   * 是否必填：是
   * 类型：String(32)
   * 示例值：1230000109
   * 描述：微信支付分配的子商户号，开发者模式下必填
   * </pre>
   */
  @XStreamAlias("sub_mch_id")
  protected String subMchId;
  /**
   * <pre>
   * 字段名：随机字符串.
   * 变量名：nonce_str
   * 是否必填：是
   * 类型：String(32)
   * 示例值：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
   * 描述：随机字符串，不长于32位。推荐随机数生成算法
   * </pre>
   */
  @XStreamAlias("nonce_str")
  protected String nonceStr;
  /**
   * <pre>
   * 字段名：签名.
   * 变量名：sign
   * 是否必填：是
   * 类型：String(32)
   * 示例值：C380BEC2BFD727A4B6845133519F3AD6
   * 描述：签名，详见签名生成算法
   * </pre>
   */
  @XStreamAlias("sign")
  protected String sign;

  /**
   * <pre>
   * 签名类型.
   * sign_type
   * 否
   * String(32)
   * HMAC-SHA256
   * 签名类型，目前支持HMAC-SHA256和MD5
   * </pre>
   */
  @XStreamAlias("sign_type")
  private String signType;

  /**
   * 将单位为元转换为单位为分.
   *
   * @param yuan 将要转换的元的数值字符串
   * @return the integer
   */
  public static Integer yuanToFen(String yuan) {
    return new BigDecimal(yuan).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
  }

  /**
   * 检查请求参数内容，包括必填参数以及特殊约束.
   */
  private void checkFields() throws WxPayException {
    //check required fields
    try {
      BeanUtils.checkRequiredFields(this);
    } catch (WxErrorException e) {
      throw new WxPayException(e.getError().getErrorMsg(), e);
    }

    //check other parameters
    this.checkConstraints();
  }

  /**
   * 检查约束情况.
   *
   * @throws WxPayException the wx pay exception
   */
  protected abstract void checkConstraints() throws WxPayException;

  /**
   * 如果配置中已经设置，可以不设置值.
   *
   * @param appid 微信公众号appid
   */
  public void setAppid(String appid) {
    this.appid = appid;
  }

  /**
   * 如果配置中已经设置，可以不设置值.
   *
   * @param mchId 微信商户号
   */
  public void setMchId(String mchId) {
    this.mchId = mchId;
  }

  /**
   * 默认采用时间戳为随机字符串，可以不设置.
   *
   * @param nonceStr 随机字符串
   */
  public void setNonceStr(String nonceStr) {
    this.nonceStr = nonceStr;
  }

  @Override
  public String toString() {
    return WxGsonBuilder.create().toJson(this);
  }

  /**
   * To xml string.
   *
   * @return the string
   */
  public String toXML() {
    XStream xstream = XStreamInitializer.getInstance();
    //涉及到服务商模式的两个参数，在为空值时置为null，以免在请求时将空值传给微信服务器
    this.setSubAppId(StringUtils.trimToNull(this.getSubAppId()));
    this.setSubMchId(StringUtils.trimToNull(this.getSubMchId()));
    xstream.processAnnotations(this.getClass());
    return xstream.toXML(this);
  }

  /**
   * 签名时，是否忽略appid.
   *
   * @return the boolean
   */
  protected boolean ignoreAppid() {
    return false;
  }

  /**
   * 签名时，忽略的参数.
   *
   * @return the string [ ]
   */
  protected String[] getIgnoredParamsForSign() {
    return new String[0];
  }

  /**
   * <pre>
   * 检查参数，并设置签名.
   * 1、检查参数（注意：子类实现需要检查参数的而外功能时，请在调用父类的方法前进行相应判断）
   * 2、补充系统参数，如果未传入则从配置里读取
   * 3、生成签名，并设置进去
   * </pre>
   *
   * @param config 支付配置对象，用于读取相应系统配置信息
   * @throws WxPayException the wx pay exception
   */
  public void checkAndSign(WxPayConfig config) throws WxPayException {
    this.checkFields();

    if (!ignoreAppid()) {
      if (StringUtils.isBlank(getAppid())) {
        this.setAppid(config.getAppId());
      }
    }

    if (StringUtils.isBlank(getMchId())) {
      this.setMchId(config.getMchId());
    }

    if (StringUtils.isBlank(getSubAppId())) {
      this.setSubAppId(config.getSubAppId());
    }

    if (StringUtils.isBlank(getSubMchId())) {
      this.setSubMchId(config.getSubMchId());
    }

    if (StringUtils.isBlank(getSignType())) {
      if (config.getSignType() != null && !ALL_SIGN_TYPES.contains(config.getSignType())) {
        throw new WxPayException("非法的signType配置：" + config.getSignType() + "，请检查配置！");
      }
      this.setSignType(StringUtils.trimToNull(config.getSignType()));
    } else {
      if (!ALL_SIGN_TYPES.contains(this.getSignType())) {
        throw new WxPayException("非法的sign_type参数：" + this.getSignType());
      }
    }

    if (StringUtils.isBlank(getNonceStr())) {
      this.setNonceStr(String.valueOf(System.currentTimeMillis()));
    }

    //设置签名字段的值
    this.setSign(SignUtils.createSign(this, this.getSignType(), config.getMchKey(), this.getIgnoredParamsForSign()));
  }
}
