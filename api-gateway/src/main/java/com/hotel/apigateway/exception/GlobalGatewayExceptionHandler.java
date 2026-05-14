package com.hotel.apigateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // handler runs before default Spring handler
public class GlobalGatewayExceptionHandler
        implements ErrorWebExceptionHandler {

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange,
                             @NonNull Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();

        /*
         * If response is already committed,
         * we cannot modify it anymore.
         */
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = determineStatus(ex);

        response.setStatusCode(status);
        response.getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "timestamp": "%s",
                  "status": %d,
                  "error": "%s",
                  "message": "%s",
                  "path": "%s"
                }
                """.formatted(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                determineMessage(status),
                exchange.getRequest().getPath()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus determineStatus(Throwable ex) {

        /*
         * Spring-generated statuses
         */
        if (ex instanceof ResponseStatusException responseStatusException) {
            return (HttpStatus) responseStatusException.getStatusCode();
        }

        /*
         * Security
         */
        if (ex instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }

        /*
         * Fallback
         */
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(HttpStatus status) {

        return switch (status) {

            case UNAUTHORIZED ->
                    "Authentication required";

            case FORBIDDEN ->
                    "Access denied";

            case NOT_FOUND ->
                    "Route not found";

            case SERVICE_UNAVAILABLE ->
                    "Service unavailable";

            default ->
                    "Unexpected gateway error";
        };
    }
}
