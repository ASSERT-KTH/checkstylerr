package com.healerjean.proj.util.rsa;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;

/**
 * @author haoshuai
 * @version 1.0v
 * @Description
 * @ClassName RSAUtils
 * @date 2019-05-28 09:59:18
 */
@Slf4j
public class RSAUtils {

    private static volatile RSAUtils rsaUtils;
    public static final String MD5withRSA = "MD5withRSA";
    public static final String SHA1withRSA = "SHA1withRSA";
    public static final String RSA = "RSA";

    private RSAUtils() {
    }

    public static RSAUtils getInstance() {
        if (null == rsaUtils) {
            synchronized (RSAUtils.class) {
                if (rsaUtils == null) {
                    rsaUtils = new RSAUtils();
                }
            }
        }
        return rsaUtils;
    }


    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        //生成一对密钥对
        KeyPair keyPair = getInstance().generatorKeyPair(4096, random);
        System.out.println(keyPair);
    }


    /**
     * 生成密钥对
     *
     * @param keysize
     * @param random
     * @return
     */
    public KeyPair generatorKeyPair(int keysize, SecureRandom random) {
        String prefixStr = "【生成密钥对】";
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(keysize, random);
            java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

            String publicKeyStr = Base64.encodeBase64String(rsaPublicKey.getEncoded());
            String privateKeyStr = Base64.encodeBase64String(rsaPrivateKey.getEncoded());

            log.info("{}Public Key：【{}】", prefixStr, publicKeyStr);
            log.info("{}Private Key：【{}】", prefixStr, privateKeyStr);
            KeyPair generatorKeyPair = new KeyPair();
            generatorKeyPair.setPublicKeyStr(publicKeyStr);
            generatorKeyPair.setPrivateKeyStr(privateKeyStr);
            return generatorKeyPair;
        } catch (NoSuchAlgorithmException e) {
            log.error("{}异常，", prefixStr, e);
        }
        return null;
    }

    /**
     * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
     *
     * @param publicKeyStr 公钥
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private PublicKey restorePublicKey(String publicKeyStr) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] publicKeyByte = Base64.decodeBase64(publicKeyStr);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        return publicKey;
    }

    /**
     * 还原私钥 PKCS8EncodedKeySpec
     *
     * @param privateKeyStr 私钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private PrivateKey restorePrivateKey(String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyByte = Base64.decodeBase64(privateKeyStr);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        return privateKey;
    }

    /**
     * 通过公钥获取秘钥属性
     *
     * @param publicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private KeyPair getKeySizeByPubKey(PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair keyPair = new KeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        RSAPublicKeySpec keySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
        BigInteger modulus = keySpec.getModulus();
        //RSA秘钥大小
        int keySize = modulus.toString(2).length();
        //RSA最大加密明文大小
        int maxEncryptSize = keySize / 8 - 11;
        //RSA最大解密密文大小
        int maxDecryptSize = keySize / 8;
        keyPair.setKeySize(keySize);
        keyPair.setMaxEncryptSize(maxEncryptSize);
        keyPair.setMaxDecryptSize(maxDecryptSize);
        return keyPair;
    }

    /**
     * 通过私钥获取秘钥属性
     *
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private KeyPair getKeySizeByPriKey(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair keyPair = new KeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        RSAPrivateKeySpec keySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
        BigInteger modulus = keySpec.getModulus();
        //RSA秘钥大小
        int keySize = modulus.toString(2).length();
        //RSA最大加密明文大小
        int maxEncryptSize = keySize / 8 - 11;
        //RSA最大解密密文大小
        int maxDecryptSize = keySize / 8;
        keyPair.setKeySize(keySize);
        keyPair.setMaxEncryptSize(maxEncryptSize);
        keyPair.setMaxDecryptSize(maxDecryptSize);
        return keyPair;
    }

    /**
     * 公钥加密
     *
     * @param original     原文
     * @param publicKeyStr 公钥
     * @return 密文
     */
    public String encryptByPubKey(String original, String publicKeyStr) {
        String prefixStr = "【公钥加密】";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (StringUtils.isEmpty(publicKeyStr)) {
                log.info("{}失败，publicKeyStr为空", prefixStr);
                return null;
            }
            byte[] originalByte = original.getBytes("UTF-8");
            if (null == originalByte) {
                log.info("{}失败，originalByte为空", prefixStr);
                return null;
            }
            PublicKey publicKey = restorePublicKey(publicKeyStr);
            KeyPair keyPair = getKeySizeByPubKey(publicKey);
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            //RSA最大加密明文大小
            int maxEncryptSize = keyPair.getMaxEncryptSize();
            int inputLen = originalByte.length;
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > maxEncryptSize) {
                    cache = cipher.doFinal(originalByte, offset, maxEncryptSize);
                } else {
                    cache = cipher.doFinal(originalByte, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * maxEncryptSize;
            }
            byte[] encryptedData = out.toByteArray();
            String ciphertext = Base64.encodeBase64String(encryptedData);
            log.debug("{}密文：【{}】", prefixStr, ciphertext);
            return ciphertext;
        } catch (Exception e) {
            log.error("{}异常，", prefixStr, e);
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("{}关闭字节数组流异常，", prefixStr, e);
                }
            }
        }
        return null;
    }

    /**
     * 私钥解密
     *
     * @param ciphertext    密文
     * @param privateKeyStr 私钥
     * @return 明文
     */
    public String decryptByPriKey(String ciphertext, String privateKeyStr) {
        String prefixStr = "【私钥解密】";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (StringUtils.isEmpty(privateKeyStr)) {
                log.info("{}失败，privateKeyStr为空", prefixStr);
                return null;
            }
            byte[] ciphertextByte = Base64.decodeBase64(ciphertext);
            if (null == ciphertextByte) {
                log.info("{}失败，ciphertextByte为空", prefixStr);
                return null;
            }
            PrivateKey privateKey = restorePrivateKey(privateKeyStr);
            KeyPair keyPair = getKeySizeByPriKey(privateKey);
            int maxDecryptSize = keyPair.getMaxDecryptSize();
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int inputLen = ciphertextByte.length;
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > maxDecryptSize) {
                    cache = cipher.doFinal(ciphertextByte, offset, maxDecryptSize);
                } else {
                    cache = cipher.doFinal(ciphertextByte, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * maxDecryptSize;
            }
            byte[] decryptedData = out.toByteArray();
            String original = new String(decryptedData, "UTF-8");
            log.debug("{}明文：【{}】", prefixStr, original);
            return original;
        } catch (Exception e) {
            log.error("{}异常，", prefixStr, e);
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("{}关闭字节数组流异常，", prefixStr, e);
                }
            }
        }
        return null;
    }


    /**
     * 私钥数字签名
     *
     * @param original      原文
     * @param privateKeyStr 私钥
     * @param algorithm     算法
     * @return 数字签名
     */
    public String signByPriKey(String original, String privateKeyStr, String algorithm) {
        String prefixStr = "【私钥数字签名】";
        try {
            byte[] privateKeyByte = Base64.decodeBase64(privateKeyStr);
            if (null == privateKeyByte) {
                log.info("{}失败，privateKeyByte为空", prefixStr);
                return null;
            }
            byte[] originalByte = original.getBytes("UTF-8");
            if (null == originalByte) {
                log.info("{}失败，originalByte为空", prefixStr);
                return null;
            }
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyByte);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(originalByte);
            byte[] signByte = signature.sign();
            String signs = Base64.encodeBase64String(signByte);
            log.debug("{}数字签名：【{}】", prefixStr, original);
            return signs;
        } catch (Exception e) {
            log.error("{}异常，", prefixStr, e);
        }
        return null;
    }

    /**
     * 公钥数字签名进行验证
     *
     * @param original     原文内容
     * @param sign         数字签名
     * @param publicKeyStr 公钥
     * @param algorithm    算法
     * @return 验签结果
     */
    public boolean verifySignByPubKey(String original, String sign, String publicKeyStr, String algorithm) {
        String prefixStr = "【公钥验证数字签名】";
        try {
            byte[] publicKeyByte = Base64.decodeBase64(publicKeyStr);
            if (null == publicKeyByte) {
                log.info("{}失败，publicKeyByte为空", prefixStr);
                return false;
            }
            byte[] originalByte = original.getBytes("UTF-8");
            if (null == originalByte) {
                log.info("{}失败，originalByte为空", prefixStr);
                return false;
            }
            byte[] signByte = Base64.decodeBase64(sign);
            if (null == signByte) {
                log.info("{}失败，signsByte为空", prefixStr);
                return false;
            }
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyByte);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(originalByte);
            boolean verify = signature.verify(signByte);
            log.info("{}验签结果：【{}】", prefixStr, verify);
            return verify;
        } catch (Exception e) {
            log.error("{}异常，", prefixStr, e);
        }
        return false;
    }
}

@Data
class KeyPair {
    /**
     * 公钥
     */
    String publicKeyStr;
    /**
     * 私钥
     */
    String privateKeyStr;
    /**
     * 秘钥keySize
     */
    int keySize;
    /**
     * 最大加密明文大小
     */
    int maxEncryptSize;
    /**
     * 最大解密密文大小
     */
    private int maxDecryptSize;
}
