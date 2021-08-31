package com.github.collector.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Sha256ChecksumProvider {

    public String checksum(final byte[] documentContents) {
        return DigestUtils.sha256Hex(documentContents);
    }
}