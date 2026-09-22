package com.ruoyi.system.domain.ai;

/**
 * 本地知识文件导入参数。
 */
public class KnowledgeImportRequest
{
    private String originalFilename;
    private byte[] content;
    private String title;
    private String authority;
    private String category;
    private String sourceUrl;

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
