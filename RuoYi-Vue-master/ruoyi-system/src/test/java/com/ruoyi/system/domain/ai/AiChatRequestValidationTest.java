package com.ruoyi.system.domain.ai;

import java.util.List;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiChatRequestValidationTest
{
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectUnsafeSessionIdAndMoreThanTwentyHistoryMessages()
    {
        AiChatRequest request = validRequest();
        request.setSessionId("../../internal session");
        request.setHistory(java.util.stream.IntStream.range(0, 21)
                .mapToObj(index -> message("user", "消息" + index))
                .toList());

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains("历史消息不能超过20条", "会话标识只能包含字母、数字、-和_");
    }

    @Test
    void shouldRejectHistoryOverSixteenThousandCharacters()
    {
        AiChatRequest request = validRequest();
        request.setHistory(List.of(
                message("user", "甲".repeat(9000)),
                message("assistant", "乙".repeat(7001))));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains("历史消息总字符不能超过16000");
    }

    private AiChatRequest validRequest()
    {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("查询库存");
        request.setSessionId("session_2026-09");
        return request;
    }

    private AiChatMessage message(String role, String content)
    {
        AiChatMessage message = new AiChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }
}
