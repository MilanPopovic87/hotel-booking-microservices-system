package com.hotel.userservice.client;

import com.hotel.userservice.dto.AuditEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audit-service", url = "http://localhost:8083")
public interface AuditClient {

    @PostMapping("/api/audit/internal")
    void sendAuditEvent(@RequestBody AuditEventRequest request);
}
