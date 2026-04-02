package com.library.common.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.library.common.constant.Constants;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 加密工具类
 */
@Component
public class EncryptionUtils {

    private AES getAes() {
        byte[] key = Constants.AES_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = Constants.AES_IV.getBytes(StandardCharsets.UTF_8);
        // 密钥需要16字节（AES-128），截取或补齐
        byte[] keyBytes = new byte[16];
        byte[] ivBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, 16));
        System.arraycopy(iv, 0, ivBytes, 0, Math.min(iv.length, 16));
        return new AES(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);
    }

    /**
     * AES加密
     */
    public String aesEncrypt(String content) {
        return getAes().encryptBase64(content);
    }

    /**
     * AES解密
     */
    public String aesDecrypt(String content) {
        return getAes().decryptStr(content);
    }

    /**
     * 密码加密（BCrypt）
     */
    public String passwordEncrypt(String password) {
        return BCryptPasswordEncoderUtils.encode(password);
    }

    /**
     * 密码验证
     */
    public boolean passwordVerify(String rawPassword, String encodedPassword) {
        return BCryptPasswordEncoderUtils.matches(rawPassword, encodedPassword);
    }
}
