package me.chanjar.weixin.open.api.impl;

import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.util.json.WxMaGsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.open.api.WxOpenComponentService;
import me.chanjar.weixin.open.api.WxOpenMaService;
import me.chanjar.weixin.open.bean.ma.WxMaOpenCommitExtInfo;
import me.chanjar.weixin.open.bean.ma.WxMaQrcodeParam;
import me.chanjar.weixin.open.bean.message.WxOpenMaSubmitAuditMessage;
import me.chanjar.weixin.open.bean.result.*;
import me.chanjar.weixin.open.util.requestexecuter.ma.MaQrCodeRequestExecutor;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/007gzs">007</a>
 * <pre>
 *     增加开放平台代小程序管理服务能力
 *     说明：这里让这个服务公开便于调用者模拟本地测试服务
 * </pre>
 * @author yqx
 * @date 2018-09-12
 */
public class WxOpenMaServiceImpl extends WxMaServiceImpl implements WxOpenMaService {
  private WxOpenComponentService wxOpenComponentService;
  private WxMaConfig wxMaConfig;
  private String appId;

  public WxOpenMaServiceImpl(WxOpenComponentService wxOpenComponentService, String appId, WxMaConfig wxMaConfig) {
    this.wxOpenComponentService = wxOpenComponentService;
    this.appId = appId;
    this.wxMaConfig = wxMaConfig;
    initHttp();
  }

  @Override
  public WxMaJscode2SessionResult jsCode2SessionInfo(String jsCode) throws WxErrorException {
    return wxOpenComponentService.miniappJscode2Session(appId, jsCode);
  }

  @Override
  public WxMaConfig getWxMaConfig() {
    return wxMaConfig;
  }

  @Override
  public String getAccessToken(boolean forceRefresh) throws WxErrorException {
    return wxOpenComponentService.getAuthorizerAccessToken(appId, forceRefresh);
  }

  /**
   * 获得小程序的域名配置信息
   *
   * @return
   */
  @Override
  public WxOpenMaDomainResult getDomain() throws WxErrorException {
    return modifyDomain("get", null, null, null, null);
  }

  /**
   * 修改服务器域名
   *
   * @param action              delete删除, set覆盖, get获取
   * @param requestdomainList
   * @param wsrequestdomainList
   * @param uploaddomainList
   * @param downloaddomainList
   * @return
   * @throws WxErrorException
   */
  public WxOpenMaDomainResult modifyDomain(String action, List<String> requestdomainList, List<String> wsrequestdomainList, List<String> uploaddomainList, List<String> downloaddomainList) throws WxErrorException {

//    if (!"get".equals(action) && (requestdomainList == null || wsrequestdomainList == null || uploaddomainList == null || downloaddomainList == null)) {
//      throw new WxErrorException(WxError.builder().errorCode(44004).errorMsg("域名参数不能为空").build());
//    }
    JsonObject requestJson = new JsonObject();
    requestJson.addProperty("action", action);
    if (!"get".equals(action)) {
      requestJson.add("requestdomain", toJsonArray(requestdomainList));
      requestJson.add("wsrequestdomain", toJsonArray(wsrequestdomainList));
      requestJson.add("uploaddomain", toJsonArray(uploaddomainList));
      requestJson.add("downloaddomain", toJsonArray(downloaddomainList));
    }
    String response = post(API_MODIFY_DOMAIN, GSON.toJson(requestJson));
    return WxMaGsonBuilder.create().fromJson(response, WxOpenMaDomainResult.class);
  }

  /**
   * 获取小程序的业务域名
   *
   * @return
   */
  @Override
  public String getWebViewDomain() throws WxErrorException {
    return setWebViewDomain("get", null);
  }

  /**
   * 设置小程序的业务域名
   *
   * @param action     add添加, delete删除, set覆盖
   * @param domainList
   * @return
   */
  @Override
  public String setWebViewDomain(String action, List<String> domainList) throws WxErrorException {
    JsonObject requestJson = new JsonObject();
    requestJson.addProperty("action", action);
    if (!"get".equals(action)) {
      requestJson.add("webviewdomain", toJsonArray(domainList));
    }
    String response = post(API_SET_WEBVIEW_DOMAIN, GSON.toJson(requestJson));
    //TODO 转化为对象返回
    return response;
  }

  /**
   * 获取小程序的信息,GET请求
   * <pre>
   *     注意：这里不能直接用小程序的access_token
   *     //TODO 待调整
   * </pre>
   *
   * @return
   * @throws WxErrorException
   */
  @Override
  public String getAccountBasicInfo() throws WxErrorException {
    String response = get(API_GET_ACCOUNT_BASICINFO, "");
    return response;
  }

  /**
   * 绑定小程序体验者
   *
   * @param wechatid 体验者微信号（不是openid）
   * @return
   * @throws WxErrorException
   */
  @Override
  public String bindTester(String wechatid) throws WxErrorException {
    JsonObject paramJson = new JsonObject();
    paramJson.addProperty("wechatid", wechatid);
    String response = post(API_BIND_TESTER, GSON.toJson(paramJson));
    return response;
  }

  /**
   * 解除绑定小程序体验者
   *
   * @param wechatid 体验者微信号（不是openid）
   * @return
   * @throws WxErrorException
   */
  @Override
  public String unbindTester(String wechatid) throws WxErrorException {
    JsonObject paramJson = new JsonObject();
    paramJson.addProperty("wechatid", wechatid);
    String response = post(API_UNBIND_TESTER, GSON.toJson(paramJson));
    return response;
  }

  /**
   * 获得体验者列表
   *
   * @return
   * @throws WxErrorException
   */
  @Override
  public WxOpenMaTesterListResult getTesterList() throws WxErrorException {
    JsonObject paramJson = new JsonObject();
    paramJson.addProperty("action", "get_experiencer");
    String response = post(API_GET_TESTERLIST, GSON.toJson(paramJson));
    return WxMaGsonBuilder.create().fromJson(response, WxOpenMaTesterListResult.class);
  }

  /**
   * 1、为授权的小程序帐号上传小程序代码
   *
   * @param templateId  代码模板ID
   * @param userVersion 用户定义版本
   * @param userDesc    用户定义版本描述
   * @param extInfo     第三方自定义的配置
   * @return
   * @throws WxErrorException
   */
  @Override
  public String codeCommit(Long templateId, String userVersion, String userDesc, WxMaOpenCommitExtInfo extInfo) throws WxErrorException {
    JsonObject params = new JsonObject();
    params.addProperty("template_id", templateId);
    params.addProperty("user_version", userVersion);
    params.addProperty("user_desc", userDesc);
    //注意：ext_json必须是字符串类型
    params.addProperty("ext_json", GSON.toJson(extInfo));
    String response = post(API_CODE_COMMIT, GSON.toJson(params));
    return response;
  }

  /**
   * 获取体验小程序的体验二维码
   *
   * @param pagePath
   * @param params
   * @return
   */
  @Override
  public File getTestQrcode(String pagePath, Map<String, String> params) throws WxErrorException {
    WxMaQrcodeParam qrcodeParam = WxMaQrcodeParam.create(pagePath);
    qrcodeParam.addPageParam(params);
    return execute(MaQrCodeRequestExecutor.create(getRequestHttp()), API_TEST_QRCODE, qrcodeParam);
  }

  /**
   * 获取授权小程序帐号的可选类目
   * <p>
   * 注意：该接口可获取已设置的二级类目及用于代码审核的可选三级类目。
   * </p>
   *
   * @return WxOpenMaCategoryListResult
   * @throws WxErrorException
   */
  @Override
  public WxOpenMaCategoryListResult getCategoryList() throws WxErrorException {
    String response = get(API_GET_CATEGORY, null);
    return WxMaGsonBuilder.create().fromJson(response, WxOpenMaCategoryListResult.class);
  }

  /**
   * 获取小程序的第三方提交代码的页面配置（仅供第三方开发者代小程序调用）
   *
   * @return
   * @throws WxErrorException
   */
  @Override
  public WxOpenMaPageListResult getPageList() throws WxErrorException {
    String response = get(API_GET_PAGE, null);
    return WxMaGsonBuilder.create().fromJson(response, WxOpenMaPageListResult.class);
  }

  /**
   * 将第三方提交的代码包提交审核（仅供第三方开发者代小程序调用）
   *
   * @param submitAuditMessage
   * @return
   * @throws WxErrorException
   */
  public WxOpenMaSubmitAuditResult submitAudit(WxOpenMaSubmitAuditMessage submitAuditMessage) throws WxErrorException {
    String response = post(API_SUBMIT_AUDIT, GSON.toJson(submitAuditMessage));
    return WxMaGsonBuilder.create().fromJson(response, WxOpenMaSubmitAuditResult.class);
  }

  /**
   * 7. 查询某个指定版本的审核状态（仅供第三方代小程序调用）
   *
   * @param auditid
   * @return
   * @throws WxErrorException
   */
  @Override
  public String getAuditStatus(Long auditid) throws WxErrorException {
    JsonObject params = new JsonObject();
    params.addProperty("auditid", auditid);
    String response = post(API_GET_AUDIT_STATUS, GSON.toJson(params));
    return response;
  }

  /**
   * 8. 查询最新一次提交的审核状态（仅供第三方代小程序调用）
   *
   * @param auditid
   * @return
   * @throws WxErrorException
   */
  @Override
  public String getLatestAuditStatus(Long auditid) throws WxErrorException {
    String response = get(API_GET_LATEST_AUDIT_STATUS, null);
    return response;
  }

  /**
   * 9. 发布已通过审核的小程序（仅供第三方代小程序调用）
   * <p>
   * 请填写空的数据包，POST的json数据包为空即可。
   * </p>
   *
   * @return
   * @throws WxErrorException
   */
  @Override
  public String releaesAudited() throws WxErrorException {
    JsonObject params = new JsonObject();
    String response = post(API_RELEASE, GSON.toJson(params));
    return response;
  }

  /**
   * 11. 小程序版本回退（仅供第三方代小程序调用）
   *
   * @return
   * @throws WxErrorException
   */
  @Override
  public String revertCodeReleaes() throws WxErrorException {
    String response = get(API_REVERT_CODE_RELEASE, null);
    return response;
  }

  /**
   * 15. 小程序审核撤回
   * <p>
   * 单个帐号每天审核撤回次数最多不超过1次，一个月不超过10次。
   * </p>
   *
   * @return
   * @throws WxErrorException
   */
  @Override
  public String undoCodeAudit() throws WxErrorException {
    String response = get(API_UNDO_CODE_AUDIT, null);
    return response;
  }

  /**
   * 将字符串对象转化为GsonArray对象
   *
   * @param strList
   * @return
   */
  private JsonArray toJsonArray(List<String> strList) {
    JsonArray jsonArray = new JsonArray();
    if (strList != null && !strList.isEmpty()) {
      for (String str : strList) {
        jsonArray.add(str);
      }
    }
    return jsonArray;
  }
}
