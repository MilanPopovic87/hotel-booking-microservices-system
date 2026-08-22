package com.hotel.apigateway.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true")
public class AiServiceAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String AI_CHAT_PATH = "/api/chat";

    private static final String INTERNAL_SERVICE_TOKEN =
            "X-Internal-Service-Token";

    private static final String AUTHENTICATED_USER =
            "X-Authenticated-User";

    private static final String AUTHENTICATED_ROLE =
            "X-Authenticated-Role";

    private final String serviceToken;

    public AiServiceAuthenticationFilter(
            @Value("${ai.service-token:}") String serviceToken) {

        if (serviceToken.isBlank()) {
            throw new IllegalStateException(
                    "AI_ENABLED=true requires AI_SERVICE_TOKEN to be configured."
            );
        }

        this.serviceToken = serviceToken;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        if (!isAiChatRequest(path)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .flatMap(authentication ->
                        forwardAuthenticatedRequest(
                                exchange,
                                chain,
                                authentication
                        )
                );
    }

    private Mono<Void> forwardAuthenticatedRequest(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            Authentication authentication) {

        String username = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("");

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {

                    // Never trust these headers if they came from the client.
                    headers.remove(INTERNAL_SERVICE_TOKEN);
                    headers.remove(AUTHENTICATED_USER);
                    headers.remove(AUTHENTICATED_ROLE);

                    // Authenticate the Gateway to the AI Chat Service.
                    headers.set(
                            INTERNAL_SERVICE_TOKEN,
                            serviceToken
                    );

                    // Forward the already authenticated user identity.
                    headers.set(
                            AUTHENTICATED_USER,
                            username
                    );

                    // Forward the user's role.
                    headers.set(
                            AUTHENTICATED_ROLE,
                            role
                    );
                }))
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isAiChatRequest(String path) {
        return path.equals(AI_CHAT_PATH)
                || path.startsWith(AI_CHAT_PATH + "/");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
