package com.ruoyi.system.domain.ai;

/**
 * 返回给 AI 的权威知识命中片段。
 */
public class KnowledgeSearchResult
{
    private String content;
    private String title;
    private String authority;
    private String url;
    private String category;
    private double relevance;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getRelevance() { return relevance; }
    public void setRelevance(double relevance) { this.relevance = relevance; }
}
