package com.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置
 */
@Configuration
public class JwtConfig {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.expiration}")
    private long expiration;

    @Value("${spring.jwt.header}")
    private String header;

    @Value("${spring.jwt.token-prefix}")
    private String tokenPrefix;

    @Bean
    public String jwtSecret() {
        return secret;
    }

    @Bean
    public long jwtExpiration() {
        return expiration;
    }

    @Bean
    public String jwtHeader() {
        return header;
    }

    @Bean
    public String jwtTokenPrefix() {
        return tokenPrefix;
    }
}