package com.ruoyi.system.domain.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * 后端确认实际命中的知识来源。
 */
public class AiCitationSource
{
    private String sourceId;
    private String title;
    private String authority;
    private String url;
    private String category;
    private List<String> citationIds = new ArrayList<>();

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getCitationIds() { return citationIds; }
    public void setCitationIds(List<String> citationIds) { this.citationIds = citationIds; }
}
