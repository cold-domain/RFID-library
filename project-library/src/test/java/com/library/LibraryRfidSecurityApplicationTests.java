package com.library;

import com.library.common.utils.BCryptPasswordEncoderUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryRfidSecurityApplicationTests {

    @Test
    void bcryptUtilityEncodesAndMatchesPassword() {
        String encodedPassword = BCryptPasswordEncoderUtils.encode("123456");

        assertThat(encodedPassword).isNotBlank();
        assertThat(BCryptPasswordEncoderUtils.matches("123456", encodedPassword)).isTrue();
    }
}
