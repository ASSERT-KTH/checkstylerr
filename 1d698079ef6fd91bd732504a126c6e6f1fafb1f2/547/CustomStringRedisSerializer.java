package com.hlj.redis.cacheSerializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * 类名称：CustomStringRedisSerializer
 * 类描述：定制String 序列转换
 * 创建人：qingxu
 * 修改人：
 * 修改时间：2016/3/2 15:04
 * 修改备注：
 *
 * @version 1.0.0
 */
public class CustomStringRedisSerializer implements RedisSerializer<Object> {

    private final Charset charset;

    public CustomStringRedisSerializer() {
        this(Charset.forName("UTF8"));
    }

    public CustomStringRedisSerializer(Charset charset) {
        Assert.notNull(charset);
        this.charset = charset;
    }

    public String deserialize(byte[] bytes) {
        return bytes == null?null:new String(bytes, this.charset);
    }

    public byte[] serialize(Object string) {
        if(string == null ) {
            return null;
        }
        if(string instanceof Long){
            return String.valueOf(string).getBytes(this.charset);

        }
        if(string instanceof Integer){
            return String.valueOf(string).getBytes(this.charset);

        }
        if(string instanceof BigDecimal){
            return ((BigDecimal)string).toString().getBytes(this.charset);
        }

        if(string instanceof BigInteger){
            return ((BigInteger)string).toString().getBytes(this.charset);
        }

        if(string instanceof Double){
            return ((Double)string).toString().getBytes(this.charset);
        }

        if(string instanceof Float){
            return ((Float)string).toString().getBytes(this.charset);
        }
        return ((String)string).getBytes(this.charset);
    }
}
