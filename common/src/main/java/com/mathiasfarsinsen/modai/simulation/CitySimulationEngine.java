package com.mathiasfarsinsen.modai.simulation;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.diplomacy.WarOutcome;
import com.mathiasfarsinsen.modai.diplomacy.WarResolver;
import com.mathiasfarsinsen.modai.naming.CityNameGenerator;
import com.mathiasfarsinsen.modai.persistence.CityManager;
import com.mathiasfarsinsen.modai.rebellion.RebellionConfig;
import com.mathiasfarsinsen.modai.rebellion.RebellionManager;
import com.mathiasfarsinsen.modai.rebellion.RebellionOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * The tick-driven simulation loop tying together resource production, war
 * resolution and rebellion checks for every registered city.
 *
 * <p>All per-city work is throttled to configurable intervals (see
 * {@link SimulationConfig}) so that a large number of cities does not cause
 * TPS drops; this class performs no blocking I/O or network calls, so it is
 * always safe to invoke from the server's main tick thread.</p>
 */
public final class CitySimulationEngine {

    private final CityManager cityManager;
    private final SimulationConfig simulationConfig;
    private final RebellionConfig rebellionConfig;
    private final ResourceProductionSystem productionSystem = new ResourceProductionSystem();
    private final WarResolver warResolver = new WarResolver();
    private final RebellionManager rebellionManager = new RebellionManager();
    private final CityNameGenerator nameGenerator;
    private final Random random;
    private final Consumer<String> eventLogger;

    public CitySimulationEngine(CityManager cityManager, SimulationConfig simulationConfig,
                                 RebellionConfig rebellionConfig, CityNameGenerator nameGenerator,
                                 Random random, Consumer<String> eventLogger) {
        this.cityManager = cityManager;
        this.simulationConfig = simulationConfig;
        this.rebellionConfig = rebellionConfig;
        this.nameGenerator = nameGenerator;
        this.random = random;
        this.eventLogger = eventLogger;
    }

    /** Advances the simulation by one server tick. Cheap to call every tick. */
    public void tick(long currentTick) {
        if (currentTick % simulationConfig.resourceProductionIntervalTicks == 0) {
            tickProduction(currentTick);
        }
        if (currentTick % simulationConfig.warResolutionIntervalTicks == 0) {
            tickWars();
        }
        if (currentTick % simulationConfig.rebellionCheckIntervalTicks == 0) {
            tickRebellions(currentTick);
        }
    }

    private void tickProduction(long currentTick) {
        for (City city : cityManager.getAllCities()) {
            productionSystem.tick(city, simulationConfig, currentTick);
        }
    }

    private void tickWars() {
        List<City> cities = new ArrayList<>(cityManager.getAllCities());
        for (int i = 0; i < cities.size(); i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                City a = cities.get(i);
                City b = cities.get(j);
                if (a.isAtWarWith(b.getId())) {
                    WarOutcome outcome = warResolver.resolveSkirmish(a, b, random);
                    log(outcome.describe());
                }
            }
        }
    }

    private void tickRebellions(long currentTick) {
        // Snapshot first: splitting a city registers a new one, and we don't
        // want to immediately re-check the freshly created splinter this cycle.
        List<City> cities = new ArrayList<>(cityManager.getAllCities());
        for (City city : cities) {
            RebellionOutcome outcome = rebellionManager.check(city, rebellionConfig, random, nameGenerator, currentTick);
            if (outcome.getType() != RebellionOutcome.Type.NONE) {
                log(outcome.describe());
                outcome.getSplinterCity().ifPresent(cityManager::addCity);
            }
        }
    }

    private void log(String message) {
        if (eventLogger != null) {
            eventLogger.accept(message);
        }
    }
}
