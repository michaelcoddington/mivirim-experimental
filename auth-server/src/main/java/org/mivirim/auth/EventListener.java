package org.mivirim.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private static final Logger LOG = LogManager.getLogger(EventListener.class);

    @org.springframework.context.event.EventListener
    void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        LOG.info("Authentication success: {}", event.getAuthentication());
    }

}
