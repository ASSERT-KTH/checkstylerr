package cn.binarywang.wx.miniapp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import cn.binarywang.wx.miniapp.bean.AbstractWxMaQrcodeWrapper;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.util.fs.FileUtils;
import me.chanjar.weixin.common.util.http.RequestExecutor;
import me.chanjar.weixin.common.util.http.RequestHttp;
import me.chanjar.weixin.common.util.http.ResponseHandler;
import me.chanjar.weixin.common.util.http.apache.InputStreamResponseHandler;
import me.chanjar.weixin.common.util.http.apache.Utf8ResponseHandler;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
public class QrcodeRequestExecutor implements RequestExecutor<File, AbstractWxMaQrcodeWrapper> {
  protected RequestHttp<CloseableHttpClient, HttpHost> requestHttp;

  public QrcodeRequestExecutor(RequestHttp requestHttp) {
    this.requestHttp = requestHttp;
  }

  @Override
  public void execute(String uri, AbstractWxMaQrcodeWrapper data, ResponseHandler<File> handler) throws WxErrorException, IOException {
    handler.handle(this.execute(uri, data));
  }

  @Override
  public File execute(String uri, AbstractWxMaQrcodeWrapper qrcodeWrapper) throws WxErrorException, IOException {
    HttpPost httpPost = new HttpPost(uri);
    if (requestHttp.getRequestHttpProxy() != null) {
      httpPost.setConfig(
        RequestConfig.custom().setProxy(requestHttp.getRequestHttpProxy()).build()
      );
    }

    httpPost.setEntity(new StringEntity(qrcodeWrapper.toJson()));

    try (final CloseableHttpResponse response = requestHttp.getRequestHttpClient().execute(httpPost);
         final InputStream inputStream = InputStreamResponseHandler.INSTANCE.handleResponse(response)) {
      Header[] contentTypeHeader = response.getHeaders("Content-Type");
      if (contentTypeHeader != null && contentTypeHeader.length > 0
        && ContentType.APPLICATION_JSON.getMimeType()
        .equals(ContentType.parse(contentTypeHeader[0].getValue()).getMimeType())) {
        String responseContent = Utf8ResponseHandler.INSTANCE.handleResponse(response);
        throw new WxErrorException(WxError.fromJson(responseContent));
      }

      return FileUtils.createTmpFile(inputStream, UUID.randomUUID().toString(), "jpg");
    } finally {
      httpPost.releaseConnection();
    }
  }
}
