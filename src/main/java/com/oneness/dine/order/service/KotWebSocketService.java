package com.oneness.dine.order.service;

import com.oneness.dine.order.dto.KotDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcasts real-time KOT and table events over WebSocket (STOMP).
 *
 * Topics:
 *   /topic/kots/{branchId}    — KOT created or status changed
 *   /topic/tables/{branchId}  — table status changed (derived from KOT/order state)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KotWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /** Broadcast a KOT event to all subscribers for this branch. */
    public void broadcastKotUpdate(Long branchId, KotDto kotDto) {
        String destination = "/topic/kots/" + branchId;
        messagingTemplate.convertAndSend(destination, kotDto);
        log.debug("WS broadcast → {} : KOT {} status {}",
                destination, kotDto.getKotNumber(), kotDto.getDisplayStatus());
    }

    /**
     * Broadcast a table-refresh signal to all subscribers for this branch.
     * Payload is a simple marker — frontend just invalidates its tables query.
     */
    public void broadcastTableUpdate(Long branchId) {
        String destination = "/topic/tables/" + branchId;
        messagingTemplate.convertAndSend(destination,
                java.util.Map.of("refresh", true, "branchId", branchId));
        log.debug("WS broadcast → {} : table refresh", destination);
    }
}
