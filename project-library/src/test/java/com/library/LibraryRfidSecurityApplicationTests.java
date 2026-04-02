package com.library;

import com.library.common.utils.BCryptPasswordEncoderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
public class LibraryRfidSecurityApplicationTests {

    @Autowired
    private BCryptPasswordEncoderUtils bCryptPasswordEncoderUtils;

    @Test
    public void contextloads(){
        String rawPassword = "123456";
        String encodedPassword = bCryptPasswordEncoderUtils.encode(rawPassword);
        System.out.println(encodedPassword);
    }
}
