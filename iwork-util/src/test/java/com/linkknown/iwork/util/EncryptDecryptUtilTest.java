package com.linkknown.iwork.util;

import com.linkknown.iwork.util.EncryptDecryptUtil;
import org.junit.Test;

public class EncryptDecryptUtilTest {

    @Test
    public void testAesEncryptToStr () {
        String encryptStr = EncryptDecryptUtil.aesEncryptToStr("helloworld", "hello");
        System.out.println(encryptStr);
    }

    @Test
    public void testAesDecryptToStr () {
        System.out.println(EncryptDecryptUtil.aesDecryptToStr("kwizcXsTicsg2wh72GyqYQ==", "hello"));
    }
}
