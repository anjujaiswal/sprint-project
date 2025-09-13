package com.sprinto.evidencebot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EscalationService {
    
    public Map<String, Object> createEscalation(String query, String reason) {
        Map<String, Object> escalation = new HashMap<>();
        escalation.put("id", System.currentTimeMillis());
        escalation.put("query", query);
        escalation.put("reason", reason);
        escalation.put("timestamp", LocalDateTime.now());
        escalation.put("status", "OPEN");
        escalation.put("message", "Escalation created for query: '" + query + "' - " + reason);
        return escalation;
    }
}