package com.ruoyi.system.ai.knowledge;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.ruoyi.system.config.AiKnowledgeProperties;

/**
 * 按自然边界切分中英文资料，并保留少量重叠上下文。
 */
@Component
public class KnowledgeTextChunker
{
    private final AiKnowledgeProperties properties;

    public KnowledgeTextChunker(AiKnowledgeProperties properties)
    {
        this.properties = properties;
    }

    public List<String> split(String rawText)
    {
        String text = normalize(rawText);
        if (text.isEmpty())
        {
            return List.of();
        }

        int chunkSize = Math.max(300, properties.getChunkSize());
        int overlap = Math.max(0, Math.min(properties.getChunkOverlap(), chunkSize / 3));
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length())
        {
            int hardEnd = Math.min(text.length(), start + chunkSize);
            int end = hardEnd == text.length() ? hardEnd : findBoundary(text, start, hardEnd);
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty())
            {
                chunks.add(chunk);
            }
            if (end >= text.length())
            {
                break;
            }
            start = Math.max(start + 1, end - overlap);
        }
        return chunks;
    }

    private int findBoundary(String text, int start, int hardEnd)
    {
        int minimumEnd = start + (int) ((hardEnd - start) * 0.65);
        for (int i = hardEnd - 1; i >= minimumEnd; i--)
        {
            char value = text.charAt(i);
            if (value == '\n' || value == '。' || value == '！' || value == '？' || value == '.' || value == ';')
            {
                return i + 1;
            }
        }
        return hardEnd;
    }

    private String normalize(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace('\u00A0', ' ')
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\t\\x0B\\f ]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
