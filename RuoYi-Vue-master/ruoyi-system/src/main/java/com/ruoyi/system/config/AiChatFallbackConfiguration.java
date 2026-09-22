package com.ruoyi.system.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ai.AiErrorCode;
import reactor.core.publisher.Flux;

/**
 * 未启用模型时提供显式失败的占位模型，让普通业务仍可正常启动。
 */
@Configuration(proxyBeanMethods = false)
public class AiChatFallbackConfiguration
{
    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    @ConditionalOnProperty(prefix = "spring.ai.model", name = "chat", havingValue = "none", matchIfMissing = true)
    public ChatModel disabledChatModel()
    {
        return new ChatModel()
        {
            @Override
            public ChatResponse call(Prompt prompt)
            {
                throw notConfigured();
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt)
            {
                return Flux.error(notConfigured());
            }

            private ServiceException notConfigured()
            {
                AiErrorCode error = AiErrorCode.AI_NOT_CONFIGURED;
                return new ServiceException(error.getMessage(), error.getCode());
            }
        };
    }
}
