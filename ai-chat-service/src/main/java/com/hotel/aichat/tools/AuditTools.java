package com.hotel.aichat.tools;

import com.hotel.aichat.client.AuditClient;
import com.hotel.aichat.dto.AuditEventResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditTools {

    private final AuditClient auditClient;

    public AuditTools(AuditClient auditClient) {
        this.auditClient = auditClient;
    }

    @Tool(description = "Returns recent audit events from the audit service")
    public List<AuditEventResponse> getRecentEvents() {

        return auditClient.getRecentEvents(5);
    }

    @Tool(description = "Returns audit events filtered by event type such as BOOKING_CREATED or BOOKING_CANCELLED")
    public List<AuditEventResponse> getEventsByType(String eventType) {

        return auditClient.getEventsByType(eventType, 5);
    }
}
