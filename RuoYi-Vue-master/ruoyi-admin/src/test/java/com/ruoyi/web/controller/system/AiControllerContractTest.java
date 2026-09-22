package com.ruoyi.web.controller.system;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import static org.assertj.core.api.Assertions.assertThat;

class AiControllerContractTest
{
    @Test
    void chatAndReportEndpointsShouldKeepSeparatePermissions() throws Exception
    {
        assertPermission(AiChatController.class.getMethod("chat",
                com.ruoyi.system.domain.ai.AiChatRequest.class), "system:ai:chat");
        assertPermission(AiChatController.class.getMethod("stream",
                com.ruoyi.system.domain.ai.AiChatRequest.class), "system:ai:chat");
        assertPermission(AiInventoryReportController.class.getMethod("data", String.class), "system:ai:report");
        assertPermission(AiInventoryReportController.class.getMethod("stream",
                com.ruoyi.system.domain.ai.AiMonthlyReportRequest.class), "system:ai:report");
    }

    @Test
    void publicRoutesShouldMatchTheDocumentedContract() throws Exception
    {
        Method data = AiInventoryReportController.class.getMethod("data", String.class);
        Method stream = AiInventoryReportController.class.getMethod("stream",
                com.ruoyi.system.domain.ai.AiMonthlyReportRequest.class);
        Method importDocument = AiKnowledgeController.class.getMethod("importDocument",
                org.springframework.web.multipart.MultipartFile.class, String.class,
                String.class, String.class, String.class);

        assertThat(data.getAnnotation(GetMapping.class).value()).containsExactly("/data");
        assertThat(stream.getAnnotation(PostMapping.class).value()).containsExactly("/stream");
        assertThat(importDocument.getAnnotation(PostMapping.class).value()).containsExactly("/import");
        assertPermission(importDocument, "system:ai:knowledge");
    }

    private void assertPermission(Method method, String permission)
    {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains(permission);
    }
}
