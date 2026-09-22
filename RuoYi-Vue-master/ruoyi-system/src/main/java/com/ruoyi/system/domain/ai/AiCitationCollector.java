package com.ruoyi.system.domain.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.StringUtils;

/**
 * 请求级来源白名单。由知识工具记录，响应阶段只读取快照。
 */
public class AiCitationCollector
{
    public static final String CONTEXT_KEY = "aiCitationCollector";

    private final Map<String, AiCitationSource> sources = new LinkedHashMap<>();

    public synchronized void record(List<KnowledgeSearchResult> results)
    {
        if (results == null)
        {
            return;
        }
        for (KnowledgeSearchResult result : results)
        {
            if (result == null || StringUtils.isBlank(result.getSourceId()))
            {
                continue;
            }
            AiCitationSource source = sources.computeIfAbsent(result.getSourceId(), key -> toSource(result));
            if (StringUtils.isNotBlank(result.getCitationId())
                    && !source.getCitationIds().contains(result.getCitationId()))
            {
                source.getCitationIds().add(result.getCitationId());
            }
        }
    }

    public synchronized List<AiCitationSource> snapshot()
    {
        return sources.values().stream().map(this::copy).toList();
    }

    private AiCitationSource toSource(KnowledgeSearchResult result)
    {
        AiCitationSource source = new AiCitationSource();
        source.setSourceId(result.getSourceId());
        source.setTitle(result.getTitle());
        source.setAuthority(result.getAuthority());
        source.setUrl(result.getUrl());
        source.setCategory(result.getCategory());
        return source;
    }

    private AiCitationSource copy(AiCitationSource source)
    {
        AiCitationSource copy = new AiCitationSource();
        copy.setSourceId(source.getSourceId());
        copy.setTitle(source.getTitle());
        copy.setAuthority(source.getAuthority());
        copy.setUrl(source.getUrl());
        copy.setCategory(source.getCategory());
        copy.setCitationIds(new ArrayList<>(source.getCitationIds()));
        return copy;
    }
}
