package com.ruoyi.system.service.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.ObjectProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ai.AiChatMessage;
import com.ruoyi.system.domain.ai.AiChatRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiChatServiceImplTest
{
    @Test
    void shouldRemoveDuplicatedCurrentMessageFromHistory()
    {
        AiChatServiceImpl service = disabledService();
        AiChatRequest request = new AiChatRequest();
        request.setMessage("查询低库存药品");
        request.setHistory(List.of(
                message("assistant", "您好"),
                message("user", "查询低库存药品")));

        List<Message> messages = service.buildHistoryMessages(request);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).isInstanceOf(AssistantMessage.class);
        assertThat(messages.get(0).getText()).isEqualTo("您好");
    }

    @Test
    void shouldKeepOnlyLatestTwentyHistoryMessages()
    {
        AiChatServiceImpl service = disabledService();
        AiChatRequest request = new AiChatRequest();
        request.setMessage("新问题");
        request.setHistory(java.util.stream.IntStream.range(0, 25)
                .mapToObj(i -> message(i % 2 == 0 ? "user" : "assistant", "消息" + i))
                .toList());

        List<Message> messages = service.buildHistoryMessages(request);

        assertThat(messages).hasSize(20);
        assertThat(messages.get(0)).isInstanceOf(AssistantMessage.class);
        assertThat(messages.get(0).getText()).isEqualTo("消息5");
        assertThat(messages.get(1)).isInstanceOf(UserMessage.class);
    }

    @Test
    void shouldReturnClearErrorWhenAiIsDisabled()
    {
        AiChatServiceImpl service = disabledService();
        AiChatRequest request = new AiChatRequest();
        request.setMessage("你好");

        assertThatThrownBy(() -> service.chat(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("AI 服务未启用");
    }

    @SuppressWarnings("unchecked")
    private AiChatServiceImpl disabledService()
    {
        ObjectProvider<ChatClient.Builder> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return new AiChatServiceImpl(provider);
    }

    private AiChatMessage message(String role, String content)
    {
        AiChatMessage message = new AiChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }
}
