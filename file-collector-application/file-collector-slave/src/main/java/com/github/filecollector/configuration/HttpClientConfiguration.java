package com.github.filecollector.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.BodyAdapter;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.adapter.ForwardingDecoder;
import com.github.mizosoft.methanol.adapter.ForwardingEncoder;
import com.github.mizosoft.methanol.adapter.jackson.JacksonAdapterFactory;
import com.google.auto.service.AutoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfiguration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @AutoService(BodyAdapter.Encoder.class)
    public static class JacksonEncoder extends ForwardingEncoder {
        public JacksonEncoder() {
            super(JacksonAdapterFactory.createEncoder(OBJECT_MAPPER));
        }
    }

    @AutoService(BodyAdapter.Decoder.class)
    public static class JacksonDecoder extends ForwardingDecoder {
        public JacksonDecoder() {
            super(JacksonAdapterFactory.createDecoder(OBJECT_MAPPER));
        }
    }

    @Bean
    public HttpClient httpClient() {
        return Methanol.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}
