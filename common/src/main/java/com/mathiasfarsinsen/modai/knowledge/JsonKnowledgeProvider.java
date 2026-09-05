package com.mathiasfarsinsen.modai.knowledge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.ResourceType;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A {@link KnowledgeProvider} backed by local JSON files ("datapack-style"
 * knowledge library). Each JSON file is an envelope object with optional
 * {@code blueprints} and {@code guides} arrays, e.g.:
 *
 * <pre>{@code
 * {
 *   "blueprints": [ { "id": "iron_farm_basic", "name": "Basic Iron Farm", ... } ],
 *   "guides": [ { "id": "housing_101", "topic": "Better Housing", "steps": [ ... ] } ]
 * }
 * }</pre>
 *
 * <p>Loading is entirely local file I/O — there is no network call involved,
 * satisfying the requirement that in-game AI decisions must not depend on
 * live internet access.</p>
 */
public final class JsonKnowledgeProvider implements KnowledgeProvider {

    private static final Logger LOGGER = Logger.getLogger(JsonKnowledgeProvider.class.getName());

    private final List<Blueprint> blueprints;
    private final List<Guide> guides;

    public JsonKnowledgeProvider(List<Blueprint> blueprints, List<Guide> guides) {
        this.blueprints = List.copyOf(blueprints);
        this.guides = List.copyOf(guides);
    }

    /**
     * Loads every {@code *.json} file found (recursively) under {@code directory}.
     * Malformed files are logged and skipped rather than aborting the whole load,
     * per the "robust fallback" requirement.
     */
    public static JsonKnowledgeProvider loadFromDirectory(Path directory) {
        List<Blueprint> blueprints = new ArrayList<>();
        List<Guide> guides = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        if (directory == null || !Files.isDirectory(directory)) {
            LOGGER.log(Level.WARNING, "Knowledge directory {0} does not exist; using empty knowledge base.",
                    directory);
            return new JsonKnowledgeProvider(blueprints, guides);
        }

        try (Stream<Path> files = Files.walk(directory)) {
            List<Path> jsonFiles = files.filter(p -> p.toString().endsWith(".json")).toList();
            for (Path file : jsonFiles) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    KnowledgeFile parsed = gson.fromJson(reader, KnowledgeFile.class);
                    if (parsed == null) {
                        continue;
                    }
                    if (parsed.blueprints != null) {
                        blueprints.addAll(parsed.blueprints);
                    }
                    if (parsed.guides != null) {
                        guides.addAll(parsed.guides);
                    }
                } catch (IOException | JsonSyntaxException e) {
                    LOGGER.log(Level.WARNING, "Failed to parse knowledge file " + file + "; skipping.", e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to walk knowledge directory " + directory + "; using what was loaded.", e);
        }

        return new JsonKnowledgeProvider(blueprints, guides);
    }

    @Override
    public List<Blueprint> getBlueprints() {
        return blueprints;
    }

    @Override
    public List<Guide> getGuides() {
        return guides;
    }

    @Override
    public Optional<Blueprint> recommendBlueprint(City city) {
        List<Blueprint> affordable = new ArrayList<>();
        for (Blueprint blueprint : blueprints) {
            if (canAfford(city, blueprint)) {
                affordable.add(blueprint);
            }
        }
        List<Blueprint> candidates = affordable.isEmpty() ? blueprints : affordable;
        return candidates.stream().max(Comparator.comparingInt(Blueprint::getTier));
    }

    private boolean canAfford(City city, Blueprint blueprint) {
        for (Map.Entry<String, Long> requirement : blueprint.getRequiredResources().entrySet()) {
            ResourceType type;
            try {
                type = ResourceType.valueOf(requirement.getKey().toUpperCase());
            } catch (IllegalArgumentException e) {
                continue; // Unknown resource key in data file; ignore rather than fail hard.
            }
            if (city.getResources().get(type) < requirement.getValue()) {
                return false;
            }
        }
        return true;
    }

    /** Internal envelope type mirroring the on-disk JSON shape. */
    private static final class KnowledgeFile {
        List<Blueprint> blueprints;
        List<Guide> guides;
    }
}
