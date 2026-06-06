package com.hotel.aichat.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditEventResponse(
        UUID eventId,
        String eventType,
        String serviceName,
        String actor,
        String entityType,
        Long entityId,
        Map<String, Object> payload,
        String message,
        LocalDateTime timestamp
) {
}
