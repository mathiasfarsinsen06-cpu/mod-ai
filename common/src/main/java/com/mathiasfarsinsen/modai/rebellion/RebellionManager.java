package com.mathiasfarsinsen.modai.rebellion;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.Citizen;
import com.mathiasfarsinsen.modai.city.Position;
import com.mathiasfarsinsen.modai.naming.CityNameGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Checks a city's stability/loyalty each simulation cycle and, when it falls
 * below the configured threshold, may trigger a rebellion.
 *
 * <p>Possible outcomes (per the spec): replace the leader, split the city
 * into two, or reduce production for a period. Exactly which outcome occurs
 * is randomized but weighted by configuration.</p>
 */
public final class RebellionManager {

    /** Stability restored after a rebellion "releases the pressure", regardless of outcome. */
    private static final int POST_REBELLION_STABILITY_RESET = 50;

    /**
     * Evaluates whether a rebellion should occur in {@code city} this cycle.
     *
     * @param currentTick the current world tick, used to schedule the end of any production penalty.
     */
    public RebellionOutcome check(City city, RebellionConfig config, Random random,
                                   CityNameGenerator nameGenerator, long currentTick) {
        if (city.getStability() > config.stabilityThreshold) {
            return RebellionOutcome.none(city);
        }
        if (random.nextDouble() >= config.triggerChance) {
            return RebellionOutcome.none(city);
        }

        boolean canSplit = city.getPopulation() >= config.minPopulationToSplit;
        if (canSplit && random.nextDouble() < config.splitChance) {
            return splitCity(city, nameGenerator, random);
        }

        // Decide between replacing the leader and a straightforward production penalty.
        if (!city.getCitizensByRole(CityRole.LEADER).isEmpty() || !city.getCitizens().isEmpty()) {
            RebellionOutcome outcome = replaceLeader(city, random);
            if (outcome != null) {
                return outcome;
            }
        }

        city.setProductionPenaltyEndTick(currentTick + config.productionPenaltyDurationTicks);
        city.setStability(POST_REBELLION_STABILITY_RESET);
        return RebellionOutcome.productionPenalty(city);
    }

    private RebellionOutcome replaceLeader(City city, Random random) {
        List<Citizen> candidates = new ArrayList<>(city.getCitizens());
        if (candidates.isEmpty()) {
            return null;
        }
        Citizen newLeader = candidates.get(random.nextInt(candidates.size()));
        for (Citizen citizen : city.getCitizens()) {
            if (citizen.getRole() == CityRole.LEADER && !citizen.equals(newLeader)) {
                citizen.setRole(CityRole.GATHERER);
            }
        }
        newLeader.setRole(CityRole.LEADER);
        city.setLeaderId(newLeader.getEntityId());
        city.setStability(POST_REBELLION_STABILITY_RESET);
        return RebellionOutcome.leaderReplaced(city, newLeader);
    }

    private RebellionOutcome splitCity(City city, CityNameGenerator nameGenerator, Random random) {
        List<Citizen> all = new ArrayList<>(city.getCitizens());
        int splitCount = Math.max(1, all.size() / 3);

        // Slightly offset the splinter city's center so it doesn't overlap the origin.
        Position origin = city.getCenter();
        Position splinterCenter = new Position(origin.getX() + 32, origin.getY(), origin.getZ() + 32);

        City splinter = new City(UUID.randomUUID(), nameGenerator.generate(), splinterCenter);
        splinter.setStability(POST_REBELLION_STABILITY_RESET);

        for (int i = 0; i < splitCount && i < all.size(); i++) {
            Citizen citizen = all.get(i);
            city.removeCitizen(citizen.getEntityId());
            if (i == 0) {
                citizen.setRole(CityRole.LEADER);
            }
            splinter.addCitizen(citizen);
        }

        city.setStability(POST_REBELLION_STABILITY_RESET);
        return RebellionOutcome.citySplit(city, splinter);
    }
}
