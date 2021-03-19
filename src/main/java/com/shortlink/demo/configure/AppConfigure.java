package com.shortlink.demo.configure;

import org.hashids.Hashids;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigure {

    @Bean
    public Hashids hashids() {
        return new Hashids("BASE#2017");
    }

}
