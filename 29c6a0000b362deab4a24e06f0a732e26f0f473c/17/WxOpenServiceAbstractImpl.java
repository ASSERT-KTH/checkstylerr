package me.chanjar.weixin.open.api.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.util.http.RequestExecutor;
import me.chanjar.weixin.common.util.http.RequestHttp;
import me.chanjar.weixin.open.api.WxOpenComponentService;
import me.chanjar.weixin.open.api.WxOpenConfigStorage;
import me.chanjar.weixin.open.api.WxOpenService;

/**
 * @author <a href="https://github.com/007gzs">007</a>
 */
public abstract class WxOpenServiceAbstractImpl<H, P> implements WxOpenService, RequestHttp<H, P> {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private WxOpenComponentService wxOpenComponentService = new WxOpenComponentServiceImpl(this);
  private WxOpenConfigStorage wxOpenConfigStorage;

  @Override
  public WxOpenComponentService getWxOpenComponentService() {
    return wxOpenComponentService;
  }

  @Override
  public WxOpenConfigStorage getWxOpenConfigStorage() {
    return wxOpenConfigStorage;
  }

  @Override
  public void setWxOpenConfigStorage(WxOpenConfigStorage wxOpenConfigStorage) {
    this.wxOpenConfigStorage = wxOpenConfigStorage;
    this.initHttp();
  }

  /**
   * 初始化 RequestHttp.
   */
  public abstract void initHttp();

  protected synchronized <T, E> T execute(RequestExecutor<T, E> executor, String uri, E data) throws WxErrorException {
    try {
      T result = executor.execute(uri, data);
      this.log.debug("\n【请求地址】: {}\n【请求参数】：{}\n【响应数据】：{}", uri, data, result);
      return result;
    } catch (WxErrorException e) {
      WxError error = e.getError();
      if (error.getErrorCode() != 0) {
        this.log.error("\n【请求地址】: {}\n【请求参数】：{}\n【错误信息】：{}", uri, data, error);
        throw new WxErrorException(error, e);
      }
      return null;
    } catch (IOException e) {
      this.log.error("\n【请求地址】: {}\n【请求参数】：{}\n【异常信息】：{}", uri, data, e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
