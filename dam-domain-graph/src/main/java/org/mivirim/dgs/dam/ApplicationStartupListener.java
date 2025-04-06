package org.mivirim.dgs.dam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.mivirim.graph.registry.GatewayRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static Logger LOG = LogManager.getLogger(ApplicationStartupListener.class);

    private GraphTraversalSource traversal;

    private GatewayRegistrar gatewayRegistrar;

    public ApplicationStartupListener(GraphTraversalSource traversal, Optional<GatewayRegistrar> registrarOptional) {
        this.traversal = traversal;
        registrarOptional.ifPresent(g -> gatewayRegistrar = g);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOG.info("Got graph traversal {}", traversal);
        if (gatewayRegistrar == null) {
            LOG.warn("No gateway registrar found; starting standalone");
        } else {
            gatewayRegistrar.register();
        }

    }

}
