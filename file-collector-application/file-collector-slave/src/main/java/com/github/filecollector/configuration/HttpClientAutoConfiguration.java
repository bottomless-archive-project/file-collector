package com.github.filecollector.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.BodyAdapter;
import com.github.mizosoft.methanol.adapter.ForwardingDecoder;
import com.github.mizosoft.methanol.adapter.ForwardingEncoder;
import com.github.mizosoft.methanol.adapter.jackson.JacksonAdapterFactory;
import com.google.auto.service.AutoService;

public class HttpClientAutoConfiguration {
    private static final ObjectMapper mapper = new ObjectMapper();

    @AutoService(BodyAdapter.Encoder.class)
    public static class JacksonEncoder extends ForwardingEncoder {
        public JacksonEncoder() {
            super(JacksonAdapterFactory.createEncoder(mapper));
        }
    }

    @AutoService(BodyAdapter.Decoder.class)
    public static class JacksonDecoder extends ForwardingDecoder {
        public JacksonDecoder() {
            super(JacksonAdapterFactory.createDecoder(mapper));
        }
    }
}
