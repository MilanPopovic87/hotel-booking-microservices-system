package com.hotel.audit.controller;

import com.hotel.audit.dto.AuditEventRequest;
import com.hotel.audit.entity.AuditEvent;
import com.hotel.audit.service.AuditService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public AuditEvent create(@RequestBody AuditEventRequest request) {

        AuditEvent event = new AuditEvent();

        event.setEventId(request.getEventId());
        event.setEventType(request.getEventType());
        event.setServiceName(request.getServiceName());
        event.setActor(request.getActor());
        event.setEntityType(request.getEntityType());
        event.setEntityId(request.getEntityId());
        event.setPayload(request.getPayload());
        event.setMessage(request.getMessage());

        return auditService.saveEvent(event);
    }
}
