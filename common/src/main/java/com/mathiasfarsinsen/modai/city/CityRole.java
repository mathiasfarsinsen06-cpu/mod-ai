package com.mathiasfarsinsen.modai.city;

/**
 * The functional role a citizen (villager) performs within a {@link City}.
 *
 * <p>This is intentionally a small, fixed set as required by the spec. New
 * roles can be added later without breaking persisted data because roles are
 * stored by their {@link #name()}.</p>
 */
public enum CityRole {
    /** Governs the city; exactly one leader per city under normal operation. */
    LEADER,
    /** Gathers raw resources (food, wood, stone, iron) over time. */
    GATHERER,
    /** Consumes resources to (eventually) construct/upgrade buildings. */
    BUILDER,
    /** Defends the city and influences war outcomes. */
    GUARD
}
