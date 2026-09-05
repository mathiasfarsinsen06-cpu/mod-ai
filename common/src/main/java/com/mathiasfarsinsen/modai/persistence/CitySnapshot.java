package com.mathiasfarsinsen.modai.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A plain-data (Gson-friendly) snapshot of a single {@link com.mathiasfarsinsen.modai.city.City},
 * used purely for JSON serialization to/from world save data. Kept separate
 * from the {@code City} domain model so that the model's invariants
 * (validation, immutability of its id, etc.) are never bypassed by the
 * deserializer.
 */
final class CitySnapshot {
    String id;
    String name;
    int centerX;
    int centerY;
    int centerZ;
    String leaderId;
    int stability;
    long productionPenaltyEndTick;
    List<CitizenSnapshot> citizens = new ArrayList<>();
    Map<String, Long> resources = new HashMap<>();
    Map<String, String> relations = new HashMap<>();

    static final class CitizenSnapshot {
        String entityId;
        String role;
    }
}
