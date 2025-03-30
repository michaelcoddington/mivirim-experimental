package org.mivirim.gateway.registry;

import org.mivirim.gateway.api.DomainInstanceInfo;

public interface DomainRegistry {

    void register(DomainInstanceInfo info);

}
