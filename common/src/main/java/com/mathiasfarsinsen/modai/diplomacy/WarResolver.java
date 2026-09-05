package com.mathiasfarsinsen.modai.diplomacy;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.ResourceType;

import java.util.Random;

/**
 * Simulates the periodic outcome of an ongoing war between two cities.
 *
 * <p>Called by the simulation engine every configured interval (e.g. every
 * X ticks) for each pair of cities currently at war. A single call resolves
 * one "skirmish": the relative military strength of each city (guards,
 * population, and iron/stone reserves) determines a winner, who inflicts
 * losses on the loser's population, resources, and stability while gaining
 * a small stability boost of its own.</p>
 */
public final class WarResolver {

    /** Fraction of the loser's population lost per skirmish. */
    private static final double POPULATION_LOSS_FRACTION = 0.10;
    /** Fraction of the loser's resources lost per skirmish. */
    private static final double RESOURCE_LOSS_FRACTION = 0.15;
    private static final int LOSER_STABILITY_PENALTY = 8;
    private static final int WINNER_STABILITY_BONUS = 2;

    /**
     * Resolves one skirmish between {@code a} and {@code b}.
     *
     * @return the outcome describing which city (if any) prevailed.
     */
    public WarOutcome resolveSkirmish(City a, City b, Random random) {
        double strengthA = militaryStrength(a);
        double strengthB = militaryStrength(b);

        if (strengthA <= 0 && strengthB <= 0) {
            // Neither side can fight; nothing happens this cycle.
            return WarOutcome.stalemate(a, b);
        }

        double total = strengthA + strengthB;
        double roll = random.nextDouble() * total;
        City winner = roll < strengthA ? a : b;
        City loser = winner == a ? b : a;

        applyLossesTo(loser);
        winner.adjustStability(WINNER_STABILITY_BONUS);

        return WarOutcome.victory(winner, loser);
    }

    private void applyLossesTo(City loser) {
        int populationLoss = (int) Math.ceil(loser.getPopulation() * POPULATION_LOSS_FRACTION);
        for (int i = 0; i < populationLoss && !loser.getCitizens().isEmpty(); i++) {
            // Prefer removing non-leader citizens first so the city can keep functioning.
            loser.getCitizens().stream()
                    .filter(c -> c.getRole() != CityRole.LEADER)
                    .findFirst()
                    .or(() -> loser.getCitizens().stream().findFirst())
                    .ifPresent(citizen -> loser.removeCitizen(citizen.getEntityId()));
        }

        for (ResourceType type : ResourceType.values()) {
            long current = loser.getResources().get(type);
            long loss = Math.round(current * RESOURCE_LOSS_FRACTION);
            loser.getResources().add(type, -loss);
        }

        loser.adjustStability(-LOSER_STABILITY_PENALTY);
    }

    private double militaryStrength(City city) {
        int guards = city.getCitizensByRole(CityRole.GUARD).size();
        double strength = guards * 3.0 + city.getPopulation() * 0.5;
        strength += city.getResources().get(ResourceType.IRON) * 0.05;
        strength += city.getResources().get(ResourceType.STONE) * 0.02;
        return Math.max(0, strength);
    }
}
