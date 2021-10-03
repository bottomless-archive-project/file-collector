package com.github.collector.configuration.crawl;

import com.github.bottomlessarchive.commoncrawl.WarcLocationFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlConfiguration {

    @Bean
    public WarcLocationFactory warcLocationFactory() {
        return new WarcLocationFactory();
    }
}
