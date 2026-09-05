package com.mathiasfarsinsen.modai.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The core model of an AI-driven city.
 *
 * <p>A {@code City} owns its citizens (villagers with a {@link CityRole}),
 * its {@link ResourceStockpile}, its diplomatic {@link RelationType}s with
 * other cities (keyed by the other city's {@link UUID}), and its loyalty
 * (a.k.a. stability) value used by the rebellion module.</p>
 *
 * <p>This class is intentionally free of any Minecraft/Forge dependency so
 * that it can be unit tested in isolation; persistence and in-world
 * representation (villager entities, POI, teleportation) are handled by the
 * Forge integration layer.</p>
 */
public final class City {

    /** Loyalty/stability is clamped to the inclusive range [0, 100]. */
    public static final int MIN_STABILITY = 0;
    public static final int MAX_STABILITY = 100;
    public static final int DEFAULT_STABILITY = 75;

    private final UUID id;
    private String name;
    private Position center;
    private UUID leaderId;
    private final List<Citizen> citizens = new ArrayList<>();
    private final ResourceStockpile resources = new ResourceStockpile();
    private final Map<UUID, RelationType> relations = new HashMap<>();
    private int stability = DEFAULT_STABILITY;
    /** World tick at which an active production penalty (from a rebellion) expires; 0 = none active. */
    private long productionPenaltyEndTick = 0;

    public City(UUID id, String name, Position center) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.center = Objects.requireNonNull(center, "center");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public Position getCenter() {
        return center;
    }

    public void setCenter(Position center) {
        this.center = Objects.requireNonNull(center, "center");
    }

    public Optional<UUID> getLeaderId() {
        return Optional.ofNullable(leaderId);
    }

    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
    }

    public List<Citizen> getCitizens() {
        return citizens;
    }

    public int getPopulation() {
        return citizens.size();
    }

    public List<Citizen> getCitizensByRole(CityRole role) {
        List<Citizen> result = new ArrayList<>();
        for (Citizen citizen : citizens) {
            if (citizen.getRole() == role) {
                result.add(citizen);
            }
        }
        return result;
    }

    /** Adds a citizen; if the role is {@link CityRole#LEADER}, also updates {@link #leaderId}. */
    public void addCitizen(Citizen citizen) {
        Objects.requireNonNull(citizen, "citizen");
        citizens.removeIf(existing -> existing.getEntityId().equals(citizen.getEntityId()));
        citizens.add(citizen);
        if (citizen.getRole() == CityRole.LEADER) {
            this.leaderId = citizen.getEntityId();
        }
    }

    public void removeCitizen(UUID entityId) {
        citizens.removeIf(c -> c.getEntityId().equals(entityId));
        if (Objects.equals(leaderId, entityId)) {
            leaderId = null;
        }
    }

    public ResourceStockpile getResources() {
        return resources;
    }

    public int getStability() {
        return stability;
    }

    public void setStability(int stability) {
        this.stability = Math.max(MIN_STABILITY, Math.min(MAX_STABILITY, stability));
    }

    public void adjustStability(int delta) {
        setStability(this.stability + delta);
    }

    public RelationType getRelation(UUID otherCityId) {
        return relations.getOrDefault(otherCityId, RelationType.PEACE);
    }

    public void setRelation(UUID otherCityId, RelationType relation) {
        relations.put(otherCityId, Objects.requireNonNull(relation, "relation"));
    }

    public boolean isAtWarWith(UUID otherCityId) {
        return getRelation(otherCityId) == RelationType.WAR;
    }

    public Map<UUID, RelationType> getRelations() {
        return relations;
    }

    public long getProductionPenaltyEndTick() {
        return productionPenaltyEndTick;
    }

    public void setProductionPenaltyEndTick(long tick) {
        this.productionPenaltyEndTick = tick;
    }

    public boolean isProductionPenalized(long currentTick) {
        return currentTick < productionPenaltyEndTick;
    }

    @Override
    public String toString() {
        return "City{" + name + ", pop=" + getPopulation() + ", stability=" + stability + '}';
    }
}
