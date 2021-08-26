package com.github.collector.view.crawl;

import com.github.collector.service.crawl.CrawlService;
import com.github.collector.view.crawl.request.InitializeCrawlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawl")
@RequiredArgsConstructor
public class CrawlController {

    private final CrawlService crawlService;

    @PostMapping
    public void initializeCrawl(@RequestBody final InitializeCrawlRequest initializeCrawlRequest) {
        crawlService.initializeCrawl(initializeCrawlRequest.getCrawlId());
    }
}
