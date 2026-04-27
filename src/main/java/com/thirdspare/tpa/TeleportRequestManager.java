package com.thirdspare.tpa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe manager for teleport requests between players.
 * Requests are stored keyed by target UUID for efficient lookup when accepting/denying.
 */
public class TeleportRequestManager {
    private final ConcurrentHashMap<UUID, List<TeleportRequest>> requestsByTarget;

    public TeleportRequestManager() {
        this.requestsByTarget = new ConcurrentHashMap<>();
    }

    /**
     * Add a new teleport request
     *
     * @param request The request to add
     */
    public void addRequest(TeleportRequest request) {
        requestsByTarget.compute(request.getTargetUUID(), (key, requests) -> {
            if (requests == null) {
                requests = new ArrayList<>();
            }
            // Remove any existing request from the same requester
            requests.removeIf(r -> r.getRequesterUUID().equals(request.getRequesterUUID()));
            // Clean up expired requests while we're here
            requests.removeIf(TeleportRequest::isExpired);
            requests.add(request);
            return requests;
        });
    }

    /**
     * Get all pending requests for a target player
     *
     * @param targetUUID The target player's UUID
     * @return List of pending (non-expired) requests, or empty list if none
     */
    public List<TeleportRequest> getRequests(UUID targetUUID) {
        List<TeleportRequest> requests = requestsByTarget.get(targetUUID);
        if (requests == null) {
            return Collections.emptyList();
        }
        // Clean up expired requests and return copy
        requests.removeIf(TeleportRequest::isExpired);
        if (requests.isEmpty()) {
            requestsByTarget.remove(targetUUID);
            return Collections.emptyList();
        }
        return new ArrayList<>(requests);
    }

    /**
     * Get the most recent pending request for a target player
     *
     * @param targetUUID The target player's UUID
     * @return The most recent request, or null if none
     */
    public TeleportRequest getMostRecentRequest(UUID targetUUID) {
        List<TeleportRequest> requests = getRequests(targetUUID);
        if (requests.isEmpty()) {
            return null;
        }
        // Return the most recently added (last in list)
        return requests.get(requests.size() - 1);
    }

    /**
     * Remove a specific request
     *
     * @param request The request to remove
     */
    public void removeRequest(TeleportRequest request) {
        requestsByTarget.computeIfPresent(request.getTargetUUID(), (key, requests) -> {
            requests.removeIf(r -> r.getRequesterUUID().equals(request.getRequesterUUID())
                    && r.getCreatedAt() == request.getCreatedAt());
            return requests.isEmpty() ? null : requests;
        });
    }

    /**
     * Check if a target player has any pending requests
     *
     * @param targetUUID The target player's UUID
     * @return true if the player has pending requests
     */
    public boolean hasRequests(UUID targetUUID) {
        return !getRequests(targetUUID).isEmpty();
    }

    /**
     * Check if a request already exists from requester to target
     *
     * @param requesterUUID The requester's UUID
     * @param targetUUID The target's UUID
     * @return true if a pending request exists
     */
    public boolean hasRequestFrom(UUID requesterUUID, UUID targetUUID) {
        List<TeleportRequest> requests = getRequests(targetUUID);
        return requests.stream().anyMatch(r -> r.getRequesterUUID().equals(requesterUUID));
    }
}
