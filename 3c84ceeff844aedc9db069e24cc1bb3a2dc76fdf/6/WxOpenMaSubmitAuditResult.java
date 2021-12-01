package me.chanjar.weixin.open.bean.result;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 微信开放平台小程序发布代码审核结果
 *
 * @author yqx
 * @date 2018/9/12
 */
@Data
public class WxOpenMaSubmitAuditResult extends WxOpenResult {

  /**
   * 审核编号
   */
  @SerializedName("auditid")
  Long auditId;

}
