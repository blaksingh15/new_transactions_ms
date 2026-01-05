
package com.example.gateway.securityconfig;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        http
//            .csrf(csrf -> csrf.disable())
//            .authorizeExchange(exchanges -> exchanges
//                .pathMatchers("/api/user/**").permitAll()
//                .anyExchange().authenticated()
//            )
//            .oauth2ResourceServer(oauth2 -> oauth2
//                .jwt() // enable JWT authentication
//            );
//
//        return http.build();
//    }
//
//    // Add this bean to fix the startup error
//    @Bean
//    public ReactiveJwtDecoder reactiveJwtDecoder() {
//        // Replace with your Authorization Server's JWK set URL
//        String jwkSetUri = "http://localhost:8080/.well-known/jwks.json";
//        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
//    }
//}

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
     SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())            
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()          
            );

        return http.build();
    }
}
