package com.tensquare.notice.config;

import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.tensquare.notice.dao")
public class MyBatisPlusConfig {
    @Bean
    public PaginationInterceptor createPaginationInterceptor() {
        return new PaginationInterceptor();
    }
}
