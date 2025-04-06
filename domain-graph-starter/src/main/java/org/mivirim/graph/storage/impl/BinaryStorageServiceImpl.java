package org.mivirim.graph.storage.impl;

import org.mivirim.graph.storage.BinaryStorageService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class BinaryStorageServiceImpl implements BinaryStorageService {

    @Override
    public Mono<Map<String, String>> storeBinaryContent(String boundary, Flux<DataBuffer> bufferFlux) {
        MultipartParser parser = new MultipartParser(boundary);
        return parser.parse(bufferFlux).then(Mono.just(Map.of()));
    }
}
