package org.mivirim.graph.storage.impl;

import com.google.common.base.Splitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

public class MultipartParserTest {

    private static final Logger LOG = LogManager.getLogger(MultipartParserTest.class);

    private Flux<DataBuffer> stringToBufferFlux(String string, int fluxSize) {

        Iterable<String> iter = Splitter.fixedLength(fluxSize).split(string);
        return Flux.fromIterable(iter).map(s -> {
            DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            LOG.info("Created data buffer: {}", s);
            DataBuffer dataBuffer = dataBufferFactory.allocateBuffer(bytes.length);
            dataBuffer.write(bytes);
            return dataBuffer;
        });

    }

    @Test
    @DisplayName("test parse simple multipart upload")
    void testSimpleMultipartUpload() {

        var upload = """
                    ----myboundary--
                    Content-Disposition: form-data; name="field1"
                    Content-Type: text/plain
                    
                    value1
                    ----myboundary--
                    Content-Disposition: form-data; name="file1"; filename="my_file.txt"
                    Content-Type: application/octet-stream
                    
                    <file data>
                    ----myboundary----
                    """
                .replaceAll("\n", "\r\n");

        var parser = new MultipartParser("--myboundary--");
        parser.parse(stringToBufferFlux(upload, 10)).subscribe();
    }

}
