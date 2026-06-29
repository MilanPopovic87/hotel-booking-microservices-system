package com.hotel.audit.kafka;

import com.hotel.audit.dto.AuditEventRequest;
import com.hotel.audit.entity.AuditEvent;
import com.hotel.audit.service.AuditService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventConsumer {

    private final AuditService auditService;

    public AuditEventConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = "audit-events", groupId = "audit-service-group")
    public void consume(AuditEventRequest request) {

        AuditEvent event = new AuditEvent();

        event.setEventId(request.getEventId());
        event.setEventType(request.getEventType());
        event.setServiceName(request.getServiceName());
        event.setActor(request.getActor());
        event.setEntityType(request.getEntityType());
        event.setEntityId(request.getEntityId());
        event.setPayload(request.getPayload());
        event.setMessage(request.getMessage());

        auditService.saveEvent(event);

        System.out.println("Received audit event: " + request.getEventId());
    }

}
