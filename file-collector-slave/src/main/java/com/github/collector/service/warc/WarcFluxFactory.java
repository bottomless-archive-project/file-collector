package com.github.collector.service.warc;

import com.github.bottomlessarchive.warc.service.WarcRecordStreamFactory;
import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecordType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URL;

@Service
public class WarcFluxFactory {

    public Flux<WarcRecord<ResponseContentBlock>> buildWarcRecordFlux(final URL warcLocation) {
        return Flux.fromStream(() -> WarcRecordStreamFactory.streamOf(warcLocation, WarcRecordType.RESPONSE));
    }
}
