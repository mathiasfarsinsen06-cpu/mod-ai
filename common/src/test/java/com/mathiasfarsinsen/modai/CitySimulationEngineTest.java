package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.ResourceType;
import com.mathiasfarsinsen.modai.diplomacy.DiplomacyManager;
import com.mathiasfarsinsen.modai.naming.CityNameGenerator;
import com.mathiasfarsinsen.modai.persistence.CityManager;
import com.mathiasfarsinsen.modai.rebellion.RebellionConfig;
import com.mathiasfarsinsen.modai.simulation.CitySimulationEngine;
import com.mathiasfarsinsen.modai.simulation.SimulationConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitySimulationEngineTest {

    @Test
    void productionRunsAutomaticallyOverMultipleTicks() {
        CityManager manager = new CityManager();
        City city = TestFixtures.newCityWithCitizens("Testburg", 1, 4, 0);
        manager.addCity(city);

        SimulationConfig simConfig = new SimulationConfig();
        simConfig.resourceProductionIntervalTicks = 10;
        List<String> events = new ArrayList<>();
        CitySimulationEngine engine = new CitySimulationEngine(manager, simConfig, new RebellionConfig(),
                new CityNameGenerator(0), new Random(0), events::add);

        for (long tick = 0; tick < 30; tick++) {
            engine.tick(tick);
        }

        assertTrue(city.getResources().get(ResourceType.WOOD) > 0);
    }

    @Test
    void warBetweenTwoCitiesEventuallyProducesLogEvent() {
        CityManager manager = new CityManager();
        City a = TestFixtures.newCityWithCitizens("Alpha", 1, 2, 10);
        City b = TestFixtures.newCityWithCitizens("Beta", 1, 2, 1);
        manager.addCity(a);
        manager.addCity(b);
        new DiplomacyManager().declareWar(a, b);

        SimulationConfig simConfig = new SimulationConfig();
        simConfig.warResolutionIntervalTicks = 5;
        List<String> events = new ArrayList<>();
        CitySimulationEngine engine = new CitySimulationEngine(manager, simConfig, new RebellionConfig(),
                new CityNameGenerator(0), new Random(1), events::add);

        for (long tick = 0; tick < 5; tick++) {
            engine.tick(tick);
        }

        assertFalse(events.isEmpty());
    }
}
