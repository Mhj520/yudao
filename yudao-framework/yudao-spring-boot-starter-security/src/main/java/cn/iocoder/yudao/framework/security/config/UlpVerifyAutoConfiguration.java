package cn.iocoder.yudao.framework.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UlpVerifyAutoConfiguration {

    @Bean
    public ComRunnerConfig comRunnerConfig() {
        return new ComRunnerConfig();
    }
}