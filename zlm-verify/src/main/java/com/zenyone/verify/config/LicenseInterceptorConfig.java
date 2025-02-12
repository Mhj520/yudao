package com.zenyone.verify.config;

import com.zenyone.verify.interceptor.LicenseVerifyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: License拦截器配置类
 *
 * @author admin
 * @version v1.0.0

 * @date created on 00:02 上午 2020/8/22
 */
@Configuration
public class LicenseInterceptorConfig implements WebMvcConfigurer {

    @Bean
    public LicenseVerifyInterceptor getLicenseCheckInterceptor() {
        return new LicenseVerifyInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.getLicenseCheckInterceptor()).addPathPatterns("/**");
    }
}
