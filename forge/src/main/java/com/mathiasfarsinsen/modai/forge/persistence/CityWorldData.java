package com.mathiasfarsinsen.modai.forge.persistence;

import com.mathiasfarsinsen.modai.persistence.CityDataCodec;
import com.mathiasfarsinsen.modai.persistence.CityManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Bridges the engine-agnostic {@link CityManager} to Minecraft's
 * {@link SavedData} world-save mechanism. All city state (names,
 * population, leaders, resources, relations, stability) is serialized to a
 * single JSON string stored in one NBT tag via {@link CityDataCodec}, so it
 * survives server restarts as required by the spec.
 */
public final class CityWorldData extends SavedData {

    private static final String DATA_NAME = "modai_cities";
    private static final String JSON_KEY = "citiesJson";

    private final CityManager cityManager;

    private CityWorldData(CityManager cityManager) {
        this.cityManager = cityManager;
    }

    public static CityWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> load(tag),
                CityWorldData::createNew,
                DATA_NAME);
    }

    private static CityWorldData createNew() {
        return new CityWorldData(new CityManager());
    }

    private static CityWorldData load(CompoundTag tag) {
        String json = tag.getString(JSON_KEY);
        return new CityWorldData(CityDataCodec.fromJson(json));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString(JSON_KEY, CityDataCodec.toJson(cityManager));
        return tag;
    }

    public CityManager getCityManager() {
        return cityManager;
    }
}
