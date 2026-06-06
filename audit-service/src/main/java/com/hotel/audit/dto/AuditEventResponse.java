package com.hotel.audit.dto;

import com.hotel.audit.entity.AuditEventType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditEventResponse(

        UUID eventId,

        AuditEventType eventType,

        String serviceName,

        String actor,

        String entityType,

        Long entityId,

        Map<String, Object> payload,

        String message,

        LocalDateTime timestamp

) {
}