package com.duodian.youhui.admin.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * @Desc:各种加密解密
 * @Author HealerJean
 * @Date 2018/5/23  下午3:12.
 */
@Slf4j
public class AesUtils {


    private static final String Loginkey = "jDFBsLGArexoYZv6";
    private static final byte[] Loginiv = "4960432827928592".getBytes();
    ;
    private static final String Loginmode = "AES/CBC/PKCS5Padding";


    public static String LoginEncrypt(String strIn) {
        try {
            SecretKeySpec skeySpec = getKey(Loginkey);
            Cipher cipher = Cipher.getInstance(Loginmode); //"算法/模式/补码方式"
            IvParameterSpec iv = new IvParameterSpec(Loginiv);////使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(strIn.getBytes());
            return new BASE64Encoder().encode(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    public static String LoginDecrypt(String strIn) {
        try {
            SecretKeySpec skeySpec = getKey(Loginkey);
            Cipher cipher = Cipher.getInstance(Loginmode);
            IvParameterSpec iv = new IvParameterSpec(Loginiv);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(strIn);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16]; // 创建一个空的16位字节数组（默认值为0）
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(arrB, "AES");
        return skeySpec;
    }


    /**
     * 微信 校验 SHA1 加密
     *
     * @param decript
     * @return
     */
    public static String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


//    public static void main(String[] args) throws Exception {
//
////        String Code = "1";
////        String codE = AesUtils.LoginEncrypt(Code);
////        System.out.println("原文：" + Code);
////        System.out.println("密文：" + codE);
////        System.out.println("解密：" + AesUtils.LoginDecrypt( codE));
//
//
//    }


    private final static String DES = "DES";
    public final static  String urlKey = "healerejean";

    /**
     * Description 根据键值进行加密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) {
        if(key==null){
            key  = urlKey ;
        }
        byte[] bt = new byte[0];
        try {
            bt = encrypt(data.getBytes(), key.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String strs = new BASE64Encoder().encode(bt);
        return strs;
    }

    /**
     * Description 根据键值进行解密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key)
    {
        if(key==null){
            key  = urlKey ;
        }

        if (data == null)
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] buf = new byte[0];
        try {
            buf = decoder.decodeBuffer(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bt = new byte[0];
        try {
            bt = decrypt(buf,key.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(bt);
    }

    /**
     * Description 根据键值进行加密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }


    /**
     * Description 根据键值进行解密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

//
//    public static void main(String[] args) throws Exception {
//
//
//        String redirectUrl = "http://web.nongfusiquan.cn/login?scope=snsapi_userinfo&fuId=4&dingId=2" ;
//        System.out.println(redirectUrl);
//
//        String data = StringCutUtils.getHttpAfter(redirectUrl);
//        System.out.println("原来的数据"+data);
//
////        String key = "HealerJean";//秘钥
//        String encode = encrypt(data, null);
//        System.err.println("加密后"+encode);
//        String dcode = decrypt(encode, null);
//        System.err.println("解密后"+dcode);
//
//    }
}
