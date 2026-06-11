package com.hotel.booking.client;

import com.hotel.booking.dto.AuditEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audit-service", url = "${services.audit.url}")
public interface AuditClient {

    @PostMapping("/api/audit/internal")
    void sendAuditEvent(@RequestBody AuditEventRequest request);
}
