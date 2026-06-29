package com.hotel.booking.kafka;

import com.hotel.booking.dto.AuditEventRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditEventProducer {

    private final KafkaTemplate<String, AuditEventRequest> kafkaTemplate;

    public AuditEventProducer(
            KafkaTemplate<String, AuditEventRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(AuditEventRequest event) {
        kafkaTemplate.send("audit-events", event);
    }
}