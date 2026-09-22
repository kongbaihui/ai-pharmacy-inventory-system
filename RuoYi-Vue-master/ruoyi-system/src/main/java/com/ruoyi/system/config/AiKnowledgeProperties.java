package com.ruoyi.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 知识库本地构建参数。
 */
@Component
@ConfigurationProperties(prefix = "pharmacy.ai.knowledge")
public class AiKnowledgeProperties
{
    private String workDir = "./runtime/ai/knowledge";
    private int maxFileSize = 25 * 1024 * 1024;
    private int chunkSize = 1200;
    private int chunkOverlap = 120;
    private int minimumSourceCount = 10;
    private boolean remoteSourcesEnabled = true;

    public String getWorkDir() { return workDir; }
    public void setWorkDir(String workDir) { this.workDir = workDir; }
    public int getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(int maxFileSize) { this.maxFileSize = maxFileSize; }
    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
    public int getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
    public int getMinimumSourceCount() { return minimumSourceCount; }
    public void setMinimumSourceCount(int minimumSourceCount) { this.minimumSourceCount = minimumSourceCount; }
    public boolean isRemoteSourcesEnabled() { return remoteSourcesEnabled; }
    public void setRemoteSourcesEnabled(boolean remoteSourcesEnabled) { this.remoteSourcesEnabled = remoteSourcesEnabled; }
}
