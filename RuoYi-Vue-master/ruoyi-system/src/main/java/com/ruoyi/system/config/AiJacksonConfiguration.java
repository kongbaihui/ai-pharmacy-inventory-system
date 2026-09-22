package com.ruoyi.system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI 运行文件仍使用 Jackson 2；Spring Boot 4 默认 JSON 映射器为 Jackson 3。
 */
@Configuration(proxyBeanMethods = false)
public class AiJacksonConfiguration
{
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper aiObjectMapper()
    {
        return new ObjectMapper().findAndRegisterModules();
    }
}
