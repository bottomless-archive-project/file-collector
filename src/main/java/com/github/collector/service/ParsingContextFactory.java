package com.github.collector.service;

import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.collector.service.domain.ParsingContext;
import org.springframework.stereotype.Service;

@Service
public class ParsingContextFactory {

    public ParsingContext buildParsingContext(final WarcRecord<ResponseContentBlock> warcRecord) {
        final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");
        final String contentString = warcRecord.getContentBlock().getPayloadAsString();

        return ParsingContext.builder()
            .baseUrl(warcRecordUrl)
            .content(contentString)
            .build();
    }
}
