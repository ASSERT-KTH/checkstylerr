package com.duodian.admore.core.helper;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 类描述：
 * 创建人：lishiwei
 * 创建时间：2017/8/23
 * 修改人：
 * 修改时间：
 * 修改备注：
 *
 * @version 1.0.0
 */
public class HeaderHelper {

    /**
     * <pre>
     * 浏览器下载文件时需要在服务端给出下载的文件名，当文件名是ASCII字符时没有问题
     * 当文件名有非ASCII字符时就有可能出现乱码
     *
     * 这里的实现方式参考这篇文章
     * http://blog.robotshell.org/2012/deal-with-http-header-encoding-for-file-download/
     *
     * 最终设置的response header是这样:
     *
     * Content-Disposition: attachment;
     *                      filename="encoded_text";
     *                      filename*=utf-8''encoded_text
     *
     * 其中encoded_text是经过RFC 3986的“百分号URL编码”规则处理过的文件名
     * </pre>
     * @param response
     * @param filename
     * @return
     */
    public static void setResponseHeader(HttpServletResponse response, String filename) throws UnsupportedEncodingException {
        String headerValue = "attachment;";
        headerValue += " filename=\"" + encodeURIComponent(filename) +"\";";
        headerValue += " filename*=utf-8''" + encodeURIComponent(filename);
        response.setHeader("Content-Disposition", headerValue);
    }

    /**
     * <pre>
     * 符合 RFC 3986 标准的“百分号URL编码”
     * 在这个方法里，空格会被编码成%20，而不是+
     * 和浏览器的encodeURIComponent行为一致
     * </pre>
     * @param value
     * @return
     */
    private static String encodeURIComponent(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20");
    }
}
