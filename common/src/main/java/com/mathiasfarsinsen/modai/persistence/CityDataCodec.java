package com.mathiasfarsinsen.modai.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mathiasfarsinsen.modai.city.Citizen;
import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.Position;
import com.mathiasfarsinsen.modai.city.RelationType;
import com.mathiasfarsinsen.modai.city.ResourceType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts between {@link City}/{@link CityManager} and a JSON representation
 * suitable for storing in a single NBT string tag (via the Forge module's
 * {@code SavedData}). This is the "persistence" module referenced in the
 * spec's architecture list.
 */
public final class CityDataCodec {

    private static final Logger LOGGER = Logger.getLogger(CityDataCodec.class.getName());
    private static final Gson GSON = new GsonBuilder().create();
    private static final Type SNAPSHOT_LIST_TYPE = new TypeToken<List<CitySnapshot>>() {}.getType();

    private CityDataCodec() {
    }

    public static String toJson(CityManager manager) {
        List<CitySnapshot> snapshots = new ArrayList<>();
        for (City city : manager.getAllCities()) {
            snapshots.add(toSnapshot(city));
        }
        return GSON.toJson(snapshots, SNAPSHOT_LIST_TYPE);
    }

    /**
     * Parses {@code json} into a {@link CityManager}. On malformed input this
     * logs a warning and returns an empty manager rather than throwing,
     * satisfying the "robust fallback" requirement.
     */
    public static CityManager fromJson(String json) {
        CityManager manager = new CityManager();
        if (json == null || json.isBlank()) {
            return manager;
        }
        try {
            List<CitySnapshot> snapshots = GSON.fromJson(json, SNAPSHOT_LIST_TYPE);
            if (snapshots == null) {
                return manager;
            }
            for (CitySnapshot snapshot : snapshots) {
                manager.addCity(fromSnapshot(snapshot));
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to parse persisted city data; starting with no cities.", e);
        }
        return manager;
    }

    private static CitySnapshot toSnapshot(City city) {
        CitySnapshot snapshot = new CitySnapshot();
        snapshot.id = city.getId().toString();
        snapshot.name = city.getName();
        snapshot.centerX = city.getCenter().getX();
        snapshot.centerY = city.getCenter().getY();
        snapshot.centerZ = city.getCenter().getZ();
        snapshot.leaderId = city.getLeaderId().map(UUID::toString).orElse(null);
        snapshot.stability = city.getStability();
        snapshot.productionPenaltyEndTick = city.getProductionPenaltyEndTick();

        for (Citizen citizen : city.getCitizens()) {
            CitySnapshot.CitizenSnapshot cs = new CitySnapshot.CitizenSnapshot();
            cs.entityId = citizen.getEntityId().toString();
            cs.role = citizen.getRole().name();
            snapshot.citizens.add(cs);
        }
        for (Map.Entry<ResourceType, Long> entry : city.getResources().asMap().entrySet()) {
            snapshot.resources.put(entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<UUID, RelationType> entry : city.getRelations().entrySet()) {
            snapshot.relations.put(entry.getKey().toString(), entry.getValue().name());
        }
        return snapshot;
    }

    private static City fromSnapshot(CitySnapshot snapshot) {
        City city = new City(UUID.fromString(snapshot.id), snapshot.name,
                new Position(snapshot.centerX, snapshot.centerY, snapshot.centerZ));
        city.setStability(snapshot.stability);
        city.setProductionPenaltyEndTick(snapshot.productionPenaltyEndTick);

        for (CitySnapshot.CitizenSnapshot cs : snapshot.citizens) {
            try {
                city.addCitizen(new Citizen(UUID.fromString(cs.entityId), CityRole.valueOf(cs.role)));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Skipping malformed citizen entry in city " + snapshot.name, e);
            }
        }
        if (snapshot.leaderId != null) {
            city.setLeaderId(UUID.fromString(snapshot.leaderId));
        }
        for (Map.Entry<String, Long> entry : snapshot.resources.entrySet()) {
            try {
                city.getResources().set(ResourceType.valueOf(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Skipping unknown resource type " + entry.getKey(), e);
            }
        }
        for (Map.Entry<String, String> entry : snapshot.relations.entrySet()) {
            try {
                city.setRelation(UUID.fromString(entry.getKey()), RelationType.valueOf(entry.getValue()));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Skipping malformed relation entry in city " + snapshot.name, e);
            }
        }
        return city;
    }
}
