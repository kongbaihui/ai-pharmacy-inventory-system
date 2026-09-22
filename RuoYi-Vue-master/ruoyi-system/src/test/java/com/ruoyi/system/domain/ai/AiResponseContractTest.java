package com.ruoyi.system.domain.ai;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class AiResponseContractTest
{
    @Test
    void expiryDateShouldSerializeAsCalendarDateWithoutTimezoneShift()
    {
        AiExpiryBatch batch = new AiExpiryBatch();
        batch.setExpireDate(LocalDate.of(2026, 11, 30));

        JsonNode json = new ObjectMapper().findAndRegisterModules().valueToTree(batch);

        assertThat(json.get("expireDate").asText()).isEqualTo("2026-11-30");
    }

    @Test
    void ordinaryResponseShouldExposeReplySessionRequestAndVerifiedSources() throws Exception
    {
        AiCitationSource source = new AiCitationSource();
        source.setSourceId("who-storage");
        source.setTitle("药品储存规范");
        source.setAuthority("WHO");
        source.setUrl("https://www.who.int/example");
        AiChatResponse response = new AiChatResponse("请按规范储存", "session-1", "request-1", List.of(source));

        JsonNode json = new ObjectMapper().valueToTree(response);

        assertThat(json.get("reply").asText()).isEqualTo("请按规范储存");
        assertThat(json.get("sessionId").asText()).isEqualTo("session-1");
        assertThat(json.get("requestId").asText()).isEqualTo("request-1");
        assertThat(json.get("sources").get(0).get("sourceId").asText()).isEqualTo("who-storage");
    }
}
