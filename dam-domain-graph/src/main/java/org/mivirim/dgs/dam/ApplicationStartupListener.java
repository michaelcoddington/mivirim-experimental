package org.mivirim.dgs.dam;

import com.netflix.appinfo.ApplicationInfoManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static Logger LOG = LogManager.getLogger(ApplicationStartupListener.class);

    @Autowired
    private GraphTraversalSource traversal;

    @Autowired
    private ApplicationInfoManager applicationInfoManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOG.info("Got graph traversal {}", traversal);
        applicationInfoManager.registerAppMetadata(Map.of("gateway-key", UUID.randomUUID().toString()));
    }

}
