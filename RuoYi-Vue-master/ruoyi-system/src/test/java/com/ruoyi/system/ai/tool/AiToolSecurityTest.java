package com.ruoyi.system.ai.tool;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import com.ruoyi.system.mapper.AiInventoryMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AiToolSecurityTest
{
    @Test
    void shouldExposeOnlyNamedReadOnlyInventoryTools()
    {
        AiInventoryTools tools = new AiInventoryTools(mock(AiInventoryMapper.class));
        Set<String> names = Arrays.stream(ToolCallbacks.from(tools))
                .map(callback -> callback.getToolDefinition().name())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(names).containsExactlyInAnyOrder(
                "searchMedicineStock", "listLowStock", "listExpiringBatches",
                "listExpiredCleanupCandidates", "getInventoryOverview",
                "getInventoryOperationsBrief", "listReplenishmentAdvice");
        assertThat(names).noneMatch(name -> name.matches("(?i).*(insert|update|delete|create|confirm|execute).*"));
    }

    @Test
    void shouldNotContainWritableOrInterpolatedSql() throws Exception
    {
        String resource = "mapper/system/AiInventoryMapper.xml";
        String xml;
        try (InputStream input = new ClassPathResource(resource).getInputStream())
        {
            xml = new String(input.readAllBytes(), StandardCharsets.UTF_8).toLowerCase();
        }

        assertThat(xml).doesNotContain("<insert", "<update", "<delete", "${");
    }

    @Test
    void everyModelToolShouldBeExplicitlyAnnotated()
    {
        assertThat(Arrays.stream(AiInventoryTools.class.getDeclaredMethods())
                .filter(method -> method.getAnnotation(Tool.class) != null)
                .map(Method::getName)
                .toList()).hasSize(7);
    }
}
