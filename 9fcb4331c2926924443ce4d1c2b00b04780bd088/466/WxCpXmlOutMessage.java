package me.chanjar.weixin.cp.bean;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import lombok.Data;
import me.chanjar.weixin.common.util.xml.XStreamCDataConverter;
import me.chanjar.weixin.cp.bean.outxmlbuilder.ImageBuilder;
import me.chanjar.weixin.cp.bean.outxmlbuilder.NewsBuilder;
import me.chanjar.weixin.cp.bean.outxmlbuilder.TextBuilder;
import me.chanjar.weixin.cp.bean.outxmlbuilder.VideoBuilder;
import me.chanjar.weixin.cp.bean.outxmlbuilder.VoiceBuilder;
import me.chanjar.weixin.cp.config.WxCpConfigStorage;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import me.chanjar.weixin.cp.util.xml.XStreamTransformer;

/**
 * 被动回复消息.
 * https://work.weixin.qq.com/api/doc#12975
 *
 * @author Daniel Qian
 */
@XStreamAlias("xml")
@Data
public abstract class WxCpXmlOutMessage implements Serializable {
  private static final long serialVersionUID = 1418629839964153110L;

  @XStreamAlias("ToUserName")
  @XStreamConverter(value = XStreamCDataConverter.class)
  protected String toUserName;

  @XStreamAlias("FromUserName")
  @XStreamConverter(value = XStreamCDataConverter.class)
  protected String fromUserName;

  @XStreamAlias("CreateTime")
  protected Long createTime;

  @XStreamAlias("MsgType")
  @XStreamConverter(value = XStreamCDataConverter.class)
  protected String msgType;

  /**
   * 获得文本消息builder.
   */
  public static TextBuilder TEXT() {
    return new TextBuilder();
  }

  /**
   * 获得图片消息builder.
   */
  public static ImageBuilder IMAGE() {
    return new ImageBuilder();
  }

  /**
   * 获得语音消息builder.
   */
  public static VoiceBuilder VOICE() {
    return new VoiceBuilder();
  }

  /**
   * 获得视频消息builder.
   */
  public static VideoBuilder VIDEO() {
    return new VideoBuilder();
  }

  /**
   * 获得图文消息builder.
   */
  public static NewsBuilder NEWS() {
    return new NewsBuilder();
  }

  protected String toXml() {
    return XStreamTransformer.toXml((Class) this.getClass(), this);
  }

  /**
   * 转换成加密的xml格式.
   */
  public String toEncryptedXml(WxCpConfigStorage wxCpConfigStorage) {
    String plainXml = toXml();
    WxCpCryptUtil pc = new WxCpCryptUtil(wxCpConfigStorage);
    return pc.encrypt(plainXml);
  }
}
