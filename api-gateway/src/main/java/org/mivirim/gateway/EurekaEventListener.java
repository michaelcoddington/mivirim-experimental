package org.mivirim.gateway;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaEventListener {

    private static final Logger LOG = LogManager.getLogger(EurekaEventListener.class);

    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        LOG.info("Got instance registered: {}, metadata {}", event, event.getInstanceInfo().getMetadata());
    }

    @EventListener
    public void onInstanceRenewed(EurekaInstanceRenewedEvent event) {
        LOG.info("Got instance renewed: {}, metadata {}", event, event.getInstanceInfo().getMetadata());
    }

    @EventListener
    public void onInstanceCanceled(EurekaInstanceCanceledEvent event) {
        LOG.info("Got instance canceled: {}", event);
    }

}
