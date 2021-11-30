package com.hlj.ddkj.Jsonp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class JsonpMain {

    @Test
    public void testGet() throws IOException {

        //data中可以放入我们想要的参数，map类型或者是其他的自己看
        //method()后面没有其他方法
        Map<String,String> data =  new HashMap<>();
        Map<String,String> headers = new HashMap<>();

        Connection.Response connection = Jsoup.connect("http://www.baidu.com")
                                                .data(data)
                                                .headers(headers)
                                                .method(Connection.Method.GET)
                                                .ignoreContentType(true)
                                                .validateTLSCertificates(false)
                                                .execute();

        String body =  connection.body();
        log.info(body);
    }

    @Test
    public void jsonNode() throws IOException {

        String json = "{\"username\":\"zhangsan\",\"性别\":\"男\",\"company\":{\"companyName\":\"中华\",\"address\":\"北京\"},\"cars\":[\"奔驰\",\"宝马\"]}";
        ObjectMapper mapper = new ObjectMapper();
        //JSON ----> JsonNode
        JsonNode rootNode = mapper.readTree(json);
        Iterator<String> keys = rootNode.fieldNames();
        while(keys.hasNext()){
            String fieldName = keys.next();
            System.out.println(fieldName + ": " + rootNode.path(fieldName).toString());
        }
        //JsonNode ----> JSON
        System.out.println(mapper.writeValueAsString(rootNode));
    }


}
