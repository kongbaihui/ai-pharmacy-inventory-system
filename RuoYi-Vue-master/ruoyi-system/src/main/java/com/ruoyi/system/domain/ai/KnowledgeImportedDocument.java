package com.ruoyi.system.domain.ai;

import java.time.OffsetDateTime;

/**
 * 持久化的本地知识文件登记项，不包含服务器绝对路径。
 */
public class KnowledgeImportedDocument
{
    private String sourceId;
    private String storageFilename;
    private String originalFilename;
    private String title;
    private String authority;
    private String category;
    private String sourceUrl;
    private String checksum;
    private OffsetDateTime importedAt;

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getStorageFilename() { return storageFilename; }
    public void setStorageFilename(String storageFilename) { this.storageFilename = storageFilename; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public OffsetDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(OffsetDateTime importedAt) { this.importedAt = importedAt; }
}
