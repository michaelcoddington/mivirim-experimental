package org.mivirim.graph;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("org.mivirim.graph.registry")
@EnableScheduling
public class RegistryAutoConfiguration {
}
