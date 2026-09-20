package com.ruoyi.system.domain.ai;

/**
 * 权威知识来源定义。
 */
public class KnowledgeSource
{
    private String id;
    private String title;
    private String authority;
    private String url;
    private String language;
    private String category;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
