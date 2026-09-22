package com.ruoyi.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class AiJacksonConfigurationTest
{
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiJacksonConfiguration.class);

    @Test
    void shouldProvideJackson2MapperWhenBootDoesNotProvideOne()
    {
        contextRunner.run(context -> assertThat(context).hasSingleBean(ObjectMapper.class));
    }
}
