package com.mathiasfarsinsen.modai.knowledge;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A structured "how to improve the city" recipe, e.g. "iron farm" or
 * "better housing". Blueprints are authored as local JSON data (see
 * {@code data/modai/knowledge/blueprints/*.json} in the Forge module) rather
 * than fetched from any live/online AI service, per the spec's safety
 * requirement.
 */
public final class Blueprint {

    private String id;
    private String name;
    private String category;
    private String description;
    private int tier = 1;
    private Map<String, Long> requiredResources = new LinkedHashMap<>();

    public Blueprint() {
        // Default constructor for Gson deserialization.
    }

    public Blueprint(String id, String name, String category, String description, int tier,
                      Map<String, Long> requiredResources) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.tier = tier;
        this.requiredResources = requiredResources;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getTier() {
        return tier;
    }

    public Map<String, Long> getRequiredResources() {
        return requiredResources == null ? Map.of() : requiredResources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blueprint)) return false;
        Blueprint blueprint = (Blueprint) o;
        return Objects.equals(id, blueprint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Blueprint{" + id + ", tier=" + tier + '}';
    }
}
