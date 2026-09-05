package com.mathiasfarsinsen.modai.simulation;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.ResourceType;

/**
 * Simulates automatic, tick-based resource gathering and consumption for a
 * single city. This intentionally does not perform real pathfinding to
 * individual blocks in this first version (per the spec, a simplified
 * simulated model is acceptable); gatherers passively produce resources
 * based on their count, and every citizen consumes food.
 */
public final class ResourceProductionSystem {

    /**
     * Runs one production interval for {@code city}.
     *
     * @param currentTick used to check for an active rebellion production penalty.
     */
    public void tick(City city, SimulationConfig config, long currentTick) {
        int gatherers = city.getCitizensByRole(CityRole.GATHERER).size();
        double penaltyMultiplier = city.isProductionPenalized(currentTick) ? 0.5 : 1.0;

        city.getResources().add(ResourceType.FOOD, (long) (gatherers * config.foodPerGatherer * penaltyMultiplier));
        city.getResources().add(ResourceType.WOOD, (long) (gatherers * config.woodPerGatherer * penaltyMultiplier));
        city.getResources().add(ResourceType.STONE, (long) (gatherers * config.stonePerGatherer * penaltyMultiplier));
        city.getResources().add(ResourceType.IRON, (long) (gatherers * config.ironPerGatherer * penaltyMultiplier));

        long foodNeeded = (long) city.getPopulation() * config.foodConsumedPerCitizen;
        boolean fed = city.getResources().consume(ResourceType.FOOD, foodNeeded);
        if (!fed) {
            // Starvation: consume whatever food remains and penalize stability instead of
            // letting the stockpile go negative.
            city.getResources().set(ResourceType.FOOD, 0);
            city.adjustStability(-3);
        }
    }
}
