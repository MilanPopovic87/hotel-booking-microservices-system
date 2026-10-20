package com.hotel.aichat.confirmation;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfirmationService {

    private final Map<String, PendingConfirmation> pendingConfirmations =
            new ConcurrentHashMap<>();

    public void createPendingConfirmation(
            String conversationId,
            PendingConfirmation confirmation) {

        pendingConfirmations.put(
                conversationId,
                confirmation
        );
    }

    public PendingConfirmation getPendingConfirmation(
            String conversationId) {

        return pendingConfirmations.get(conversationId);
    }

    public boolean consumeIfMatches(
            String conversationId,
            PendingConfirmation requested) {

        return pendingConfirmations.remove(
                conversationId,
                requested
        );
    }

    public void clearConfirmation(
            String conversationId) {

        pendingConfirmations.remove(conversationId);
    }

}