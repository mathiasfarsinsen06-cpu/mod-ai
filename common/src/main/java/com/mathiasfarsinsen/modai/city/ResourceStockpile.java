package com.mathiasfarsinsen.modai.city;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks a city's stockpile of {@link ResourceType}s.
 *
 * <p>All mutation methods clamp values at zero so that consumption never
 * produces negative stockpiles (a fallback/robustness requirement from the
 * spec), and are safe to call from the tick loop.</p>
 */
public final class ResourceStockpile {

    private final Map<ResourceType, Long> amounts = new EnumMap<>(ResourceType.class);

    public ResourceStockpile() {
        for (ResourceType type : ResourceType.values()) {
            amounts.put(type, 0L);
        }
    }

    public long get(ResourceType type) {
        return amounts.getOrDefault(type, 0L);
    }

    /** Adds {@code amount} (may be negative) to the stockpile, clamped to a minimum of zero. */
    public void add(ResourceType type, long amount) {
        long updated = get(type) + amount;
        amounts.put(type, Math.max(0L, updated));
    }

    public void set(ResourceType type, long amount) {
        amounts.put(type, Math.max(0L, amount));
    }

    /**
     * Attempts to consume {@code amount} of the given resource.
     *
     * @return {@code true} if there was enough of the resource and it was consumed,
     *         {@code false} if the stockpile was insufficient (nothing is consumed).
     */
    public boolean consume(ResourceType type, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (get(type) < amount) {
            return false;
        }
        add(type, -amount);
        return true;
    }

    public Map<ResourceType, Long> asMap() {
        return new EnumMap<>(amounts);
    }

    public void loadFromMap(Map<ResourceType, Long> values) {
        for (Map.Entry<ResourceType, Long> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }
}
