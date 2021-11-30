package com.github.binarywang.wxpay.bean.result;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * 微信支付-申请退款返回结果.
 * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4
 * </pre>
 *
 * @author liukaitj & Binary Wang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XStreamAlias("xml")
public class WxPayRefundResult extends BaseWxPayResult implements Serializable {
  private static final long serialVersionUID = -3392333879907788033L;
  /**
   * 微信订单号.
   */
  @XStreamAlias("transaction_id")
  private String transactionId;

  /**
   * 商户订单号.
   */
  @XStreamAlias("out_trade_no")
  private String outTradeNo;

  /**
   * 商户退款单号.
   */
  @XStreamAlias("out_refund_no")
  private String outRefundNo;

  /**
   * 微信退款单号.
   */
  @XStreamAlias("refund_id")
  private String refundId;

  /**
   * 退款金额.
   */
  @XStreamAlias("refund_fee")
  private Integer refundFee;

  /**
   * 应结退款金额.
   */
  @XStreamAlias("settlement_refund_fee")
  private Integer settlementRefundFee;

  /**
   * 标价金额.
   */
  @XStreamAlias("total_fee")
  private Integer totalFee;

  /**
   * 应结订单金额.
   */
  @XStreamAlias("settlement_total_fee")
  private Integer settlementTotalFee;

  /**
   * 标价币种.
   */
  @XStreamAlias("fee_type")
  private String feeType;

  /**
   * 现金支付金额.
   */
  @XStreamAlias("cash_fee")
  private Integer cashFee;

  /**
   * 现金支付币种.
   */
  @XStreamAlias("cash_fee_type")
  private String cashFeeType;

  /**
   * 现金退款金额.
   */
  @XStreamAlias("cash_refund_fee")
  private String cashRefundFee;

  /**
   * 退款代金券使用数量.
   */
  @XStreamAlias("coupon_refund_count")
  private Integer couponRefundCount;

  private List<WxPayRefundCouponInfo> refundCoupons;

  /**
   * 组装生成退款代金券信息.
   */
  public void composeRefundCoupons() {
    List<WxPayRefundCouponInfo> coupons = Lists.newArrayList();
    for (int i = 0; i < this.getCouponRefundCount(); i++) {
      coupons.add(
        new WxPayRefundCouponInfo(
          this.getXmlValue("xml/coupon_refund_id_" + i),
          this.getXmlValueAsInt("xml/coupon_refund_fee_" + i),
          this.getXmlValue("xml/coupon_type_" + i)
        )
      );
    }
    this.setRefundCoupons(coupons);
  }
}
