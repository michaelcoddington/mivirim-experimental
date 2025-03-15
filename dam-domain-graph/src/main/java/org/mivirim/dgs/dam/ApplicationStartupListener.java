package org.mivirim.dgs.dam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static Logger LOG = LogManager.getLogger(ApplicationStartupListener.class);

    @Autowired
    private GraphTraversalSource traversal;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOG.info("Got graph traversal {}", traversal);
    }

}
