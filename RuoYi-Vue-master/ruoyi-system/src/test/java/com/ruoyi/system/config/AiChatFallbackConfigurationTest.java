package com.ruoyi.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ai.AiErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiChatFallbackConfigurationTest
{
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiChatFallbackConfiguration.class);

    @Test
    void shouldProvideExplicitlyDisabledModelWhenProviderIsNone()
    {
        contextRunner.withPropertyValues("spring.ai.model.chat=none").run(context ->
        {
            assertThat(context).hasSingleBean(ChatModel.class);
            ChatModel model = context.getBean(ChatModel.class);
            assertThatThrownBy(() -> model.call(new Prompt("test")))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(AiErrorCode.AI_NOT_CONFIGURED.getCode());
        });
    }

    @Test
    void shouldBackOffWhenRealProviderIsSelected()
    {
        contextRunner.withPropertyValues("spring.ai.model.chat=openai")
                .run(context -> assertThat(context).doesNotHaveBean(ChatModel.class));
    }
}
