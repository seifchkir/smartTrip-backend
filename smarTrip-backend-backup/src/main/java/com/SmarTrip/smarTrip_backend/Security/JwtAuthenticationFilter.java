package com.SmarTrip.smarTrip_backend.Security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Your JWT authentication logic here
         //Example:
         String token = exchange.getRequest().getHeaders().getFirst("Authorization");
         if (token != null && token.startsWith("Bearer ")) {
             // Validate token and set authentication
         }
        
        return chain.filter(exchange);
    }
}
