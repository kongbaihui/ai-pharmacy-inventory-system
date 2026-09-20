package com.ruoyi.system.domain.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 可索引的知识文本切片。
 */
public class KnowledgeChunk
{
    private String id;
    private String content;
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
