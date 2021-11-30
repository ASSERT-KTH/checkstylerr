package com.hlj.util.Z010异常;

import org.junit.Test;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName MainTest
 * @date 2019/5/30  16:25.
 * @Description
 */
public class MainTest {


    /**
     * code:301----msg:null
     */
    @Test
    public void code(){
        try {
            throw new BusinessException(ResponseEnum.参数错误.getCode());
        }catch (BusinessException b){
            System.out.println("code:"+b.getCode() +"----msg:"+b.getMessage());
        }
    }

    /**
     * code:301----msg:异常信息
     */
    @Test
    public void message(){
        try {
            throw new BusinessException("异常信息");
        }catch (BusinessException b){
            System.out.println("code:"+b.getCode() +"----msg:"+b.getMessage());
        }
    }


    /**
     * code:1----msg:异常是1
     */
    @Test
    public void codeAndMessage(){
        try {
            throw new BusinessException(1,"异常是1");
        }catch (BusinessException b){
            System.out.println("code:"+b.getCode() +"----msg:"+b.getMessage());
        }
    }

    /**
     * code:400----msg:参数错误
     */
    @Test
    public void EnumTest(){
        try {
            throw new BusinessException(ResponseEnum.参数错误);
        }catch (BusinessException b){
            System.out.println("code:"+b.getCode() +"----msg:"+b.getMessage());
        }
    }

    /**
     *  code:1----msg:java.lang.ArithmeticException: / by zero
     */
    @Test
    public void codeAndException(){
        try {
            int i = 1/0 ;
        }catch (Exception e){
            try {
                throw new BusinessException(1,e);
            }catch (BusinessException b){
                System.out.println("code:"+b.getCode() +"----msg:"+b.getMessage());
            }
        }
    }

    /**
     * code:301----msg:异常信息 (默认提供了code)
     */
    @Test
    public void messageAndException(){
        try {
            int i = 1/0 ;
        }catch (Exception e){
            try {
                throw new BusinessException("异常信息",e);
            }catch (BusinessException b){
                System.out.println("code:"+b.getCode() +"----msg:"+b.getMessage());
            }
        }
    }


}
