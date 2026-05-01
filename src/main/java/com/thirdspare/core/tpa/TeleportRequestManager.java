package com.thirdspare.core.tpa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportRequestManager {
    private final ConcurrentHashMap<UUID, List<TeleportRequest>> requestsByTarget;

    public TeleportRequestManager() {
        this.requestsByTarget = new ConcurrentHashMap<>();
    }

    public void addRequest(TeleportRequest request) {
        requestsByTarget.compute(request.getTargetUUID(), (key, requests) -> {
            if (requests == null) {
                requests = new ArrayList<>();
            }
            requests.removeIf(r -> r.getRequesterUUID().equals(request.getRequesterUUID()));
            requests.removeIf(TeleportRequest::isExpired);
            requests.add(request);
            return requests;
        });
    }

    public List<TeleportRequest> getRequests(UUID targetUUID) {
        List<TeleportRequest> requests = requestsByTarget.get(targetUUID);
        if (requests == null) {
            return Collections.emptyList();
        }
        requests.removeIf(TeleportRequest::isExpired);
        if (requests.isEmpty()) {
            requestsByTarget.remove(targetUUID);
            return Collections.emptyList();
        }
        return new ArrayList<>(requests);
    }

    public TeleportRequest getMostRecentRequest(UUID targetUUID) {
        List<TeleportRequest> requests = getRequests(targetUUID);
        if (requests.isEmpty()) {
            return null;
        }
        return requests.get(requests.size() - 1);
    }

    public void removeRequest(TeleportRequest request) {
        requestsByTarget.computeIfPresent(request.getTargetUUID(), (key, requests) -> {
            requests.removeIf(r -> r.getRequesterUUID().equals(request.getRequesterUUID())
                    && r.getCreatedAt() == request.getCreatedAt());
            return requests.isEmpty() ? null : requests;
        });
    }

    public boolean hasRequests(UUID targetUUID) {
        return !getRequests(targetUUID).isEmpty();
    }

    public boolean hasRequestFrom(UUID requesterUUID, UUID targetUUID) {
        List<TeleportRequest> requests = getRequests(targetUUID);
        return requests.stream().anyMatch(r -> r.getRequesterUUID().equals(requesterUUID));
    }
}
