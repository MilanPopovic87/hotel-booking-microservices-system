package com.hotel.audit.controller;

import com.hotel.audit.entity.AuditEvent;
import com.hotel.audit.entity.AuditEventType;
import com.hotel.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
@TestPropertySource(properties = {"internal.service-token=test-token"})
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @Test
    void shouldCreateAuditEvent() throws Exception {

        UUID eventId = UUID.randomUUID();

        AuditEvent event = createEvent(eventId, AuditEventType.BOOKING_CREATED);

        when(auditService.saveEvent(any(AuditEvent.class)))
                .thenReturn(event);

        mockMvc.perform(
                        post("/api/audit/internal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "eventId": "%s",
                                    "eventType": "BOOKING_CREATED",
                                    "serviceName": "booking-service",
                                    "actor": "admin",
                                    "entityType": "BOOKING",
                                    "entityId": 10,
                                    "payload": {
                                        "roomId": 5
                                    },
                                    "message": "Booking created"
                                }
                                """.formatted(eventId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.eventType").value("BOOKING_CREATED"))
                .andExpect(jsonPath("$.serviceName").value("booking-service"))
                .andExpect(jsonPath("$.actor").value("admin"))
                .andExpect(jsonPath("$.entityType").value("BOOKING"))
                .andExpect(jsonPath("$.entityId").value(10))
                .andExpect(jsonPath("$.message").value("Booking created"));
    }

    @Test
    void shouldReturnRecentEvents() throws Exception {

        UUID eventId = UUID.randomUUID();

        AuditEvent event = createEvent(
                eventId,
                AuditEventType.BOOKING_CREATED
        );

        when(auditService.getRecentEvents(10))
                .thenReturn(List.of(event));

        mockMvc.perform(
                        get("/api/audit/internal/ai/events/recent")
                                .param("limit", "10")
                                .header("X-Internal-Service-Token", "test-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$[0].eventType")
                        .value("BOOKING_CREATED"))
                .andExpect(jsonPath("$[0].serviceName")
                        .value("booking-service"))
                .andExpect(jsonPath("$[0].actor")
                        .value("admin"));
    }

    @Test
    void shouldReturnEventsByType() throws Exception {

        UUID eventId = UUID.randomUUID();

        AuditEvent event = createEvent(
                eventId,
                AuditEventType.BOOKING_CANCELLED
        );

        when(auditService.getEventsByType(
                eq(AuditEventType.BOOKING_CANCELLED),
                eq(10)
        )).thenReturn(List.of(event));

        mockMvc.perform(
                        get("/api/audit/internal/ai/events/by-type/BOOKING_CANCELLED")
                                .param("limit", "10")
                                .header("X-Internal-Service-Token", "test-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$[0].eventType")
                        .value("BOOKING_CANCELLED"))
                .andExpect(jsonPath("$[0].message")
                        .value("Booking cancelled"));
    }

    private AuditEvent createEvent(
            UUID eventId,
            AuditEventType eventType
    ) {

        AuditEvent event = new AuditEvent();

        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setServiceName("booking-service");
        event.setActor("admin");
        event.setEntityType("BOOKING");
        event.setEntityId(10L);
        event.setPayload(Map.of("roomId", 5));
        event.setMessage(
                eventType == AuditEventType.BOOKING_CANCELLED
                        ? "Booking cancelled"
                        : "Booking created"
        );
        event.setTimestamp(LocalDateTime.of(2026, 8, 11, 18, 0));
        event.setVersion(1);

        return event;
    }
}
