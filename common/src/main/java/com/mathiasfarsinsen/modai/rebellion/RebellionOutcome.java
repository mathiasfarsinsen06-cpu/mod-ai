package com.mathiasfarsinsen.modai.rebellion;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.Citizen;

import java.util.Optional;

/**
 * Describes what happened as a result of a {@link RebellionManager} check.
 */
public final class RebellionOutcome {

    public enum Type {
        NONE,
        LEADER_REPLACED,
        CITY_SPLIT,
        PRODUCTION_PENALTY
    }

    private final Type type;
    private final City originCity;
    private final City splinterCity;
    private final Citizen newLeader;

    private RebellionOutcome(Type type, City originCity, City splinterCity, Citizen newLeader) {
        this.type = type;
        this.originCity = originCity;
        this.splinterCity = splinterCity;
        this.newLeader = newLeader;
    }

    public static RebellionOutcome none(City city) {
        return new RebellionOutcome(Type.NONE, city, null, null);
    }

    public static RebellionOutcome leaderReplaced(City city, Citizen newLeader) {
        return new RebellionOutcome(Type.LEADER_REPLACED, city, null, newLeader);
    }

    public static RebellionOutcome citySplit(City originCity, City splinterCity) {
        return new RebellionOutcome(Type.CITY_SPLIT, originCity, splinterCity, null);
    }

    public static RebellionOutcome productionPenalty(City city) {
        return new RebellionOutcome(Type.PRODUCTION_PENALTY, city, null, null);
    }

    public Type getType() {
        return type;
    }

    public City getOriginCity() {
        return originCity;
    }

    public Optional<City> getSplinterCity() {
        return Optional.ofNullable(splinterCity);
    }

    public Optional<Citizen> getNewLeader() {
        return Optional.ofNullable(newLeader);
    }

    public String describe() {
        switch (type) {
            case LEADER_REPLACED:
                return "Rebellion in " + originCity.getName() + ": the leader was overthrown and replaced.";
            case CITY_SPLIT:
                return "Rebellion in " + originCity.getName() + ": a faction split off to found "
                        + splinterCity.getName() + ".";
            case PRODUCTION_PENALTY:
                return "Unrest in " + originCity.getName() + " has reduced production.";
            default:
                return originCity.getName() + " remains stable.";
        }
    }
}
