package org.mivirim.graph.storage;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface BinaryStorageService {

    Mono<Map<String, String>> storeBinaryContent(String boundary, Flux<DataBuffer> bufferFlux);

}
