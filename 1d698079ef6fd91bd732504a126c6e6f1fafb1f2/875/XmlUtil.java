package com.hlj.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Desc: 微信消息格式以及xml和对象的转化
 * @Date:  2018/5/24 下午5:54.


    <!--对象转xml-->
    <dependency>
        <groupId>com.thoughtworks.xstream</groupId>
        <artifactId>xstream</artifactId>
        <version>1.4.9</version>
    </dependency>

    <!--将request流转化为map-->
    <dependency>
        <groupId>dom4j</groupId>
        <artifactId>dom4j</artifactId>
        <version>1.6.1</version>
    </dependency>

 */

public class XmlUtil {



    /**
     * 将xml转化为Map集合
     *
     * @param request
     * @return
     */
    public static Map<String, String> xmlToMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        SAXReader reader = new SAXReader();
        InputStream ins = null;
        try {
            ins = request.getInputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Document doc = null;
        try {
            doc = reader.read(ins);
        } catch (DocumentException e1) {
            e1.printStackTrace();
        }
        Element root = doc.getRootElement();
        @SuppressWarnings("unchecked")
        List<Element> list = root.elements();
        for (Element e : list) {
            map.put(e.getName(), e.getText());
        }
        try {
            ins.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return map;
    }



    /**
     * XML格式字符串转map
     */
    public static Map<String, String> xmlToMap(String xmlStr) {
        Map<String, String> map = null;
        try {
            map = new HashMap<String, String>();
            Document document = DocumentHelper.parseText(xmlStr);
            Element root = document.getRootElement();
            List<Element> elementList = root.elements();
            for (Element e : elementList)
                map.put(e.getName(), e.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return map;
    }



    /**
     * 文本消息转化为xml
     *
     * @param object
     * @return
     */
    public static String objectToXml(Object object) {
        XStream xstream = new XStream(new DomDriver("utf-8"));
        //默认是包名加类名为标签，这里改为xml为标签
        xstream.alias("xml", object.getClass());
        //默认是包名加类名为标签，这里改为item为标签
//        xstream.alias("item", new NewsArticle().getClass());
        //这里遇到了一点点挫折，原来官网文档错了
//        xstream.alias("Image", new Image().getClass());

        return xstream.toXML(object);

    }

    /**
     * Map转换为xml
     * @param map
     * @return
     */
    public static String map2XmlString(Map<String, String> map) {
        String xmlResult = "";

        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (String key : map.keySet()) {
            System.out.println(key + "========" + map.get(key));

            String value = "<![CDATA[" + map.get(key) + "]]>";
            sb.append("<" + key + ">" + value + "</" + key + ">");
            System.out.println();
        }
        sb.append("</xml>");
        xmlResult = sb.toString();

        return xmlResult;
    }


    /**
     * SortedMap转换为xml
     * @param parameters
     * @return
     */
    public static String SortedMap2XmlString(SortedMap<Object, Object> parameters)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k))
            {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else
            {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }





    /**
     * xml转json
     * @param xmlStr
     * @return
     * @throws DocumentException
     */
    public static JSONObject xml2JSONObject(String xmlStr)  {
        Document doc= null;
        try {
            doc = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        JSONObject json=new JSONObject();
        dom4j2Json(doc.getRootElement(), json);
        return json;
    }


    /**
     * xml转json 调用方法
     * @param element
     * @param json
     */
    public static void dom4j2Json(Element element,JSONObject json){
        //如果是属性
        for(Object o:element.attributes()){
            Attribute attr=(Attribute)o;
            if(!isEmpty(attr.getValue())){
                json.put("@"+attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl=element.elements();
        if(chdEl.isEmpty()&&!isEmpty(element.getText())){//如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for(Element e:chdEl){//有子元素
            if(!e.elements().isEmpty()){//子元素也有子元素
                JSONObject chdjson=new JSONObject();
                dom4j2Json(e,chdjson);
                Object o=json.get(e.getName());
                if(o!=null){
                    JSONArray jsona=null;
                    if(o instanceof JSONObject){//如果此元素已存在,则转为jsonArray
                        JSONObject jsono=(JSONObject)o;
                        json.remove(e.getName());
                        jsona=new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if(o instanceof JSONArray){
                        jsona=(JSONArray)o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                }else{
                    if(!chdjson.isEmpty()){
                        json.put(e.getName(), chdjson);
                    }
                }


            }else{//子元素没有子元素
                for(Object o:element.attributes()){
                    Attribute attr=(Attribute)o;
                    if(!isEmpty(attr.getValue())){
                        json.put("@"+attr.getName(), attr.getValue());
                    }
                }
                if(!e.getText().isEmpty()){
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }

    public static boolean isEmpty(String str) {

        if (str == null || str.trim().isEmpty() || "null".equals(str)) {
            return true;
        }
        return false;
    }


    // 微信不正规xml转map，没有版本号
/*    <xml>
        <mch_appid>wxe062425f740c30d8</mch_appid>
        <mchid>10000098</mchid>
        <nonce_str>3PG2J4ILTKCH16CQ2502SI8ZNMTM67VS</nonce_str>
        <partner_trade_no>100000982014120919616</partner_trade_no>
        <openid>ohO4Gt7wVPxIT1A9GjFaMYMiZY1s</openid>
        <check_name>FORCE_CHECK</check_name>
        <re_user_name>张三</re_user_name>
        <amount>100</amount>
        <desc>节日快乐!</desc>
        <spbill_create_ip>10.2.3.10</spbill_create_ip>
        <sign>C97BDBACF37622775366F38B629F45E3</sign>
    </xml>
*/

    /**
     * XML格式字符串转换为Map
     *
     * @param strXML XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public static Map<String, String> wechatXmlToMap(String strXML)  {
        try {
            Map<String, String> data = new HashMap<String, String>();
            DocumentBuilder documentBuilder = newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                org.w3c.dom.Node node = nodeList.item(idx);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            try {
                stream.close();
            } catch (Exception ex) {
                ExceptionLogUtils.log(ex,XmlUtil.class);
            }
            return data;
        } catch (Exception ex) {
            ExceptionLogUtils.log(ex,XmlUtil.class);
        }

        return null;
    }

    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        return documentBuilderFactory.newDocumentBuilder();
    }





}