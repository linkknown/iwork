package com.linkknown.iwork.config;

import com.linkknown.iwork.filter.IPFilter;
import com.linkknown.iwork.filter.LoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Autowired
    private IworkConfig iworkConfig;

    @Bean
    public FilterRegistrationBean registIpFilter () {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new IPFilter(iworkConfig));
        registration.addUrlPatterns("/*");
        registration.addInitParameter("exclusions", "/api/iwork/httpservice/*,/api/iwork/login");
        registration.setName("IPFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean registLoginFilter () {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new LoginFilter(iworkConfig));
        registration.addUrlPatterns("/*");
        registration.addInitParameter("exclusions", "/api/iwork/httpservice/*,/api/iwork/login");
        registration.setName("LoginFilter");
        registration.setOrder(2);
        return registration;
    }
}
