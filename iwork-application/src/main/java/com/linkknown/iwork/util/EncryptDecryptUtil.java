package com.linkknown.iwork.util;

public class EncryptDecryptUtil {

    public static String aesEncryptToStr(String decryptStr, String key) {
        return AESUtil.aesEncryptToStr(decryptStr, key);
    }

    public static String aesDecryptToStr(String encryptStr, String key) {
        return AESUtil.aesDecryptToStr(encryptStr, key);
    }
}
