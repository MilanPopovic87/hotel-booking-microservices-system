package com.hotel.apigateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AiServiceAvailabilityFilter implements GlobalFilter, Ordered {

    private static final String AI_CHAT_PATH = "/api/chat";

    private final boolean aiEnabled;

    public AiServiceAvailabilityFilter(
            @Value("${ai.enabled}") boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
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

        if (!aiEnabled) {
            ServerHttpResponse response = exchange.getResponse();

            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String body = """
            {
              "status": 503,
              "code": "AI_SERVICE_NOT_ENABLED",
              "message": "AI service is not enabled."
            }
            """;

            DataBuffer buffer = response.bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    private boolean isAiChatRequest(String path) {
        return path.equals(AI_CHAT_PATH)
                || path.startsWith(AI_CHAT_PATH + "/");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
