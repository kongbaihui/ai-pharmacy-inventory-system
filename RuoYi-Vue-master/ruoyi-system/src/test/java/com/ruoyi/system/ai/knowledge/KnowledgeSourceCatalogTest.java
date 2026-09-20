package com.ruoyi.system.ai.knowledge;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.system.domain.ai.KnowledgeSource;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeSourceCatalogTest
{
    @Test
    void shouldContainUniqueHttpsSourcesFromApprovedAuthorities() throws Exception
    {
        List<KnowledgeSource> sources;
        try (InputStream input = new ClassPathResource("ai/knowledge-sources.json").getInputStream())
        {
            sources = new ObjectMapper().readValue(input, new TypeReference<>() { });
        }

        assertThat(sources).hasSizeGreaterThanOrEqualTo(15);
        Set<String> ids = new HashSet<>();
        Set<String> hosts = Set.of("www.nhc.gov.cn", "www.samr.gov.cn", "sjfg.samr.gov.cn",
                "www.gov.cn", "english.nmpa.gov.cn", "cdn.who.int", "iris.who.int",
                "medlineplus.gov",
                "zjjcmspublic.oss-cn-hangzhou-zwynet-d01-a.internet.cloud.zj.gov.cn");
        for (KnowledgeSource source : sources)
        {
            URI uri = URI.create(source.getUrl());
            assertThat(source.getId()).matches("[a-z0-9-]+");
            assertThat(ids.add(source.getId())).isTrue();
            assertThat(uri.getScheme()).isEqualTo("https");
            assertThat(hosts).contains(uri.getHost());
            assertThat(source.getAuthority()).isNotBlank();
            assertThat(source.getCategory()).isNotBlank();
        }
    }
}
