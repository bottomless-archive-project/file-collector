package com.github.collector.view.crawl;

import com.github.collector.view.crawl.request.InitializeCrawlRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawl")
public class CrawlController {

    @PostMapping
    public void initializeCrawl(@RequestBody final InitializeCrawlRequest initializeCrawlRequest) {

    }
}
