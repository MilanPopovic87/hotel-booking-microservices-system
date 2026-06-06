package com.hotel.aichat.client;

import com.hotel.aichat.dto.AuditEventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "audit-service", url = "${services.audit.url}")
public interface AuditClient {

    @GetMapping("/api/audit/internal/events/recent")
    List<AuditEventResponse> getRecentEvents(@RequestParam int limit);

    @GetMapping("/api/audit/internal/events/by-type/{type}")
    List<AuditEventResponse> getEventsByType(@PathVariable("type") String eventType, @RequestParam int limit);

}
