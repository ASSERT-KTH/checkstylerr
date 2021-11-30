package com.hlj.ddkj.ExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author fengchuanbo
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> {

    /**
     * 返回code
     */
    private String code;
    /**
     * 返回描述
     */
    private String desc;
    /**
     * 返回数据
     */
    private T data;

    public Response(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Response(Code code) {
        this.code = code.getCode();
        this.desc = code.getDesc();
    }

    public Response(Code code, T data) {
        this.code = code.getCode();
        this.desc = code.getDesc();
        this.data = data;
    }

    /**
     * 成功响应
     * @param t
     * @param <T>
     * @return
     */
    public static <T> Response<T> success(T t){
        return new Response<>(Code.OK, t);
    }

    /**
     * 成功响应，date为空
     * @return
     */
    public static Response success(){
        return new Response(Code.OK);
    }

    /**
     * 参数错误
     * @return
     */
    public static Response illegalArgument(){
        return new Response(Code.illegalArgument);
    }


    /**
     * 自定义返回
     * @param code
     * @param desc
     * @return
     */
    public static <T> Response of(String code,String desc, T t){
        return new Response(code,desc,t);
    }


    /**
     * 自定义返回
     * @param code
     * @param desc
     * @return
     */
    public static Response of(String code,String desc){
        return new Response(code,desc);
    }


    /**
     * 自定义返回
     * @param code
     * @param t
     * @return
     */
    public static <T> Response of(Code code, T t){
        return new Response(code,t);
    }

    /**
     * 自定义返回
     * @param code
     * @return
     */
    public static Response of(Code code){
        return new Response(code);
    }


    /**
     * 系统错误
     * @return
     */
    public static Response error() {
        return new Response(Code.ERROR);
    }

}
