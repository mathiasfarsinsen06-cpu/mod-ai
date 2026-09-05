package com.mathiasfarsinsen.modai.city;

import java.util.Objects;
import java.util.UUID;

/**
 * A lightweight, engine-agnostic reference to a villager entity that belongs
 * to a {@link City}.
 *
 * <p>Only the entity's {@link UUID} and its {@link CityRole} are stored here;
 * the actual Minecraft {@code Villager} entity lookup happens in the Forge
 * integration layer so that this module has no engine dependency.</p>
 */
public final class Citizen {

    private final UUID entityId;
    private CityRole role;

    public Citizen(UUID entityId, CityRole role) {
        this.entityId = Objects.requireNonNull(entityId, "entityId");
        this.role = Objects.requireNonNull(role, "role");
    }

    public UUID getEntityId() {
        return entityId;
    }

    public CityRole getRole() {
        return role;
    }

    public void setRole(CityRole role) {
        this.role = Objects.requireNonNull(role, "role");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Citizen)) return false;
        Citizen citizen = (Citizen) o;
        return entityId.equals(citizen.entityId);
    }

    @Override
    public int hashCode() {
        return entityId.hashCode();
    }

    @Override
    public String toString() {
        return "Citizen{" + entityId + ", role=" + role + '}';
    }
}
