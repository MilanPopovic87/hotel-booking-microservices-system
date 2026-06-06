package com.hotel.audit.service;

import com.hotel.audit.entity.AuditEvent;
import com.hotel.audit.entity.AuditEventType;
import com.hotel.audit.repository.AuditEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public AuditEvent saveEvent(AuditEvent event) {

        try {
            // Audit service is the source of truth for event time
            // (we never trust client-provided timestamps)
            event.setTimestamp(LocalDateTime.now());

            // Default version for event schema evolution
            // Used later when event structure changes over time
            if (event.getVersion() == null) {
                event.setVersion(1);
            }

            // Attempt to persist event
            return auditEventRepository.save(event);

        } catch (DataIntegrityViolationException e) {

            // Idempotency safety:
            // If another request already saved the same eventId,
            // DB will reject duplicate primary key insert.
            // In that case, return the existing record.

            return auditEventRepository.findById(event.getEventId())
                    .orElseThrow(() ->
                            new IllegalStateException("Event conflict detected but not found: " + event.getEventId(), e)
                    );
        }
    }

    public List<AuditEvent> getRecentEvents(int limit) {

        limit = Math.min(limit, 50);

        return auditEventRepository.findAllByOrderByTimestampDesc( PageRequest.of(0, limit));

    }

    public List<AuditEvent> getEventsByType(AuditEventType eventType, int limit) {

        limit = Math.min(limit, 50);

        return auditEventRepository
                .findByEventTypeOrderByTimestampDesc(eventType,  PageRequest.of(0, limit));

    }
}