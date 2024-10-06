package com.gym.gymsystem.config;

import com.gym.gymsystem.filter.CredentialsValidationFilter;
import com.gym.gymsystem.service.UserService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<CredentialsValidationFilter> credentialsValidationFilter(UserService userService) {
        FilterRegistrationBean<CredentialsValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CredentialsValidationFilter(userService));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
