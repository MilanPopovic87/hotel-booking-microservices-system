package com.hotel.audit.controller;

import com.hotel.audit.dto.AuditEventRequest;
import com.hotel.audit.dto.AuditEventResponse;
import com.hotel.audit.entity.AuditEvent;
import com.hotel.audit.entity.AuditEventType;
import com.hotel.audit.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit/internal")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    private AuditEventResponse mapToResponse(
            AuditEvent event
    ) {
        return new AuditEventResponse(
                event.getEventId(),
                event.getEventType(),
                event.getServiceName(),
                event.getActor(),
                event.getEntityType(),
                event.getEntityId(),
                event.getPayload(),
                event.getMessage(),
                event.getTimestamp()
        );
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

    @GetMapping("/events/recent")
    public List<AuditEventResponse> getRecentEvents(@RequestParam(defaultValue = "20") int limit) {

        return auditService.getRecentEvents(limit)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/events/by-type/{type}")
    public List<AuditEventResponse> getEventsByType(@PathVariable AuditEventType type,
                                                    @RequestParam(defaultValue = "20") int limit) {

        return auditService.getEventsByType(type, limit)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
