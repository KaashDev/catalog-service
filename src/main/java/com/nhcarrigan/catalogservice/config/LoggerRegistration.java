package com.nhcarrigan.catalogservice.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nhcarrigan.catalogservice.filter.LoggingFilter;

@Configuration
public class LoggerRegistration {

    @Bean
    public FilterRegistrationBean<LoggingFilter> logFilter() {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/api/products", "/api/products/*");
        return registrationBean;
    }
}
