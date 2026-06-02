package com.hotel.booking.dto;

import java.util.Map;
import java.util.UUID;

public class AuditEventRequest {

    private UUID eventId;

    private AuditEventType eventType;

    private String serviceName;

    private String actor;

    private String entityType;

    private Long entityId;

    private Map<String, Object> payload;

    private String message;

    public AuditEventRequest() {
    }

    public AuditEventRequest(
            UUID eventId,
            AuditEventType eventType,
            String serviceName,
            String actor,
            String entityType,
            Long entityId,
            Map<String, Object> payload,
            String message) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.serviceName = serviceName;
        this.actor = actor;
        this.entityType = entityType;
        this.entityId = entityId;
        this.payload = payload;
        this.message = message;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
