package com.zenyone.core.config;

import com.zenyone.core.helper.LoggerHelper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>license-core模块中的Bean实现自动装配 -- 配置类</p>
 *
 * @author admin
 * @version v1.0.0

 * @date created on 10:24 下午 2020/8/21
 */
@Configuration
@ComponentScan(basePackages = {"com.zenyone.core"})
public class LicenseCoreAutoConfigure {
    public LicenseCoreAutoConfigure(){
        LoggerHelper.info("============ license-core-spring-boot-starter initialization！ ===========");
    }
}
