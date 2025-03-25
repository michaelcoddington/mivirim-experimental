package org.mivirim.gateway.registry.impl;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mivirim.gateway.registry.DomainRegistry;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DomainRegistryImpl implements DomainRegistry {

    private static final Logger LOG = LogManager.getLogger(DomainRegistryImpl.class);

    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        LOG.info("Got instance registered: {}, metadata {}", event, event.getInstanceInfo().getMetadata());
        InstanceInfo info = event.getInstanceInfo();
        loadDomainSchema(info);
    }

    @EventListener
    public void onInstanceRenewed(EurekaInstanceRenewedEvent event) {
        LOG.info("Got instance renewed: {}, metadata {}", event, event.getInstanceInfo().getMetadata());
        InstanceInfo info = event.getInstanceInfo();
        loadDomainSchema(info);
    }

    @EventListener
    public void onInstanceCanceled(EurekaInstanceCanceledEvent event) {
        LOG.info("Got instance canceled: {}", event);
    }

    private void loadDomainSchema(InstanceInfo info) {
        WebClient webClient = WebClient.create(String.format("http://%s:%d/graphql", info.getHostName(), info.getPort()));
        WebClientGraphQLClient client = MonoGraphQLClient.createWithWebClient(webClient);

        var query = """
                    query IntrospectionQuery {
                       __schema {
                         queryType { name }
                         mutationType { name }
                         subscriptionType { name }
                         types { ...FullType }
                         directives {
                           name
                           description
                           locations
                           args { ...InputValue }
                         }
                       }
                     }
                
                     fragment FullType on __Type {
                       kind
                       name
                       description
                       fields(includeDeprecated: true) {
                         name
                         description
                         args { ...InputValue }
                         type { ...TypeRef }
                         isDeprecated
                         deprecationReason
                       }
                       inputFields { ...InputValue }
                       interfaces { ...TypeRef }
                       enumValues(includeDeprecated: true) {
                         name
                         description
                         isDeprecated
                         deprecationReason
                       }
                       possibleTypes { ...TypeRef }
                     }
                
                     fragment InputValue on __InputValue {
                       name
                       description
                       type { ...TypeRef }
                       defaultValue
                     }
                
                     fragment TypeRef on __Type {
                       kind
                       name
                       ofType {
                         kind
                         name
                         ofType {
                           kind
                           name
                           ofType {
                             kind
                             name
                             ofType {
                               kind
                               name
                               ofType {
                                 kind
                                 name
                                 ofType {
                                   kind
                                   name
                                   ofType {
                                     kind
                                     name
                                     ofType {
                                       kind
                                       name
                                       ofType {
                                         kind
                                         name
                                       }
                                     }
                                   }
                                 }
                               }
                             }
                           }
                         }
                       }
                     }
                """;

        //The GraphQLResponse contains data and errors.
        Mono<GraphQLResponse> graphQLResponseMono = client.reactiveExecuteQuery(query);
        graphQLResponseMono.subscribe(response -> {
            LOG.info("Got introspection response {}", response);
        });
    }

}
