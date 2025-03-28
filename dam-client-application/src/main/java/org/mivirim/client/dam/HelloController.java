package org.mivirim.client.dam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/hello")
public class HelloController {

    private static final Logger LOG = LogManager.getLogger(HelloController.class);

    @GetMapping
    public Mono<String> sayHello(@AuthenticationPrincipal Mono<OAuth2AuthenticationToken> authMono) {

        return authMono.map(auth -> {
            return "hi, I'm the client and you are " + auth;
        }).switchIfEmpty(Mono.just("whatever dude"));

    }

}
