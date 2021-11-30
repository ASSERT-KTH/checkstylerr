package com.hlj.netty.websocket.redis.cacheSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 类描述：redis用ObjectMapper
 * 创建人： j.sh
 * 创建时间： 2016/8/29
 * version：1.0.0
 */
public class RedisObjectMapper extends ObjectMapper {

    public RedisObjectMapper() {
        super();
        super.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }
}
