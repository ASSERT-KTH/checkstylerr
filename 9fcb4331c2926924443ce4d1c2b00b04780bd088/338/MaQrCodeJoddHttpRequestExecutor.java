package me.chanjar.weixin.open.util.requestexecuter.ma;

import jodd.http.HttpConnectionProvider;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.ProxyInfo;
import jodd.util.MimeTypes;
import jodd.util.StringPool;
import me.chanjar.weixin.common.WxType;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.util.fs.FileUtils;
import me.chanjar.weixin.common.util.http.RequestHttp;
import me.chanjar.weixin.open.bean.ma.WxMaQrcodeParam;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * @author yqx
 * @date 2018-09-13
 */
public class MaQrCodeJoddHttpRequestExecutor extends MaQrCodeRequestExecutor<HttpConnectionProvider, ProxyInfo> {
  public MaQrCodeJoddHttpRequestExecutor(RequestHttp requestHttp) {
    super(requestHttp);
  }

  @Override
  public File execute(String uri, WxMaQrcodeParam qrcodeParam) throws WxErrorException, IOException {
    if (qrcodeParam != null && StringUtils.isNotBlank(qrcodeParam.getPagePath())) {
      if (uri.indexOf('?') == -1) {
        uri += '?';
      }
      uri += uri.endsWith("?")
        ? "path=" + URLEncoder.encode(qrcodeParam.getRequestPath(), "UTF-8")
        : "&path=" + URLEncoder.encode(qrcodeParam.getRequestPath(), "UTF-8");
    }


    HttpRequest request = HttpRequest.get(uri);
    if (requestHttp.getRequestHttpProxy() != null) {
      requestHttp.getRequestHttpClient().useProxy(requestHttp.getRequestHttpProxy());
    }
    request.withConnectionProvider(requestHttp.getRequestHttpClient());

    HttpResponse response = request.send();
    response.charset(StringPool.UTF_8);
    String contentTypeHeader = response.header("Content-Type");
    if (MimeTypes.MIME_TEXT_PLAIN.equals(contentTypeHeader)) {
      String responseContent = response.bodyText();
      throw new WxErrorException(WxError.fromJson(responseContent, WxType.MiniApp));
    }
    try (InputStream inputStream = new ByteArrayInputStream(response.bodyBytes())) {
      return FileUtils.createTmpFile(inputStream, UUID.randomUUID().toString(), "jpg");
    }
  }
}
