package com.mathiasfarsinsen.modai.persistence;

import com.mathiasfarsinsen.modai.city.City;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory registry of all known cities, keyed by their {@link UUID}.
 *
 * <p>This class holds no Minecraft/Forge dependency; the Forge module is
 * responsible for wrapping instances of this class in a {@code SavedData}
 * so they persist across server restarts (see {@link CityDataCodec} for the
 * JSON (de)serialization used for that purpose).</p>
 */
public final class CityManager {

    private final Map<UUID, City> cities = new LinkedHashMap<>();

    public void addCity(City city) {
        cities.put(city.getId(), city);
    }

    public void removeCity(UUID id) {
        cities.remove(id);
    }

    public Optional<City> getCity(UUID id) {
        return Optional.ofNullable(cities.get(id));
    }

    /** Case-insensitive lookup by city name. */
    public Optional<City> getCityByName(String name) {
        return cities.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Collection<City> getAllCities() {
        return cities.values();
    }

    public int size() {
        return cities.size();
    }
}
