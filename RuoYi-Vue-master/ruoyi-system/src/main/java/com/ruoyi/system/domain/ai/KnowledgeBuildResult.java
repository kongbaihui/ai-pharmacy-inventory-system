package com.ruoyi.system.domain.ai;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识语料构建结果。
 */
public class KnowledgeBuildResult
{
    private String buildId;
    private String status;
    private int sourceCount;
    private int successCount;
    private int failedCount;
    private int chunkCount;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private Map<String, String> failures = new LinkedHashMap<>();

    public String getBuildId() { return buildId; }
    public void setBuildId(String buildId) { this.buildId = buildId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getSourceCount() { return sourceCount; }
    public void setSourceCount(int sourceCount) { this.sourceCount = sourceCount; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(OffsetDateTime finishedAt) { this.finishedAt = finishedAt; }
    public Map<String, String> getFailures() { return failures; }
    public void setFailures(Map<String, String> failures) { this.failures = failures; }
}
