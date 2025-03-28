package org.mivirim.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    /*
    @ConditionalOnMissingBean
    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http.securityMatcher("/articles/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest()
                        .hasAuthority("SCOPE_articles.read"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

     */

    @ConditionalOnMissingBean
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /*
                .authorizeHttpRequests(customizer ->
                        customizer.requestMatchers("/**")
                                .permitAll()
                );

                 */
                .authorizeHttpRequests(customizer ->
                        customizer.anyRequest().permitAll()
                ).csrf(customizer -> customizer.disable());

                                /*
                                .anyRequest().hasAuthority("SCOPE_articles.read"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
                );
                */

                /*
                .securityMatcher("/articles/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest()
                        .hasAuthority("SCOPE_articles.read"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

                 */
        return http.build();
    }



}
