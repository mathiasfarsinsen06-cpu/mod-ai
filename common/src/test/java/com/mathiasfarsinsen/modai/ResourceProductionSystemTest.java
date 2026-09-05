package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.ResourceType;
import com.mathiasfarsinsen.modai.simulation.ResourceProductionSystem;
import com.mathiasfarsinsen.modai.simulation.SimulationConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceProductionSystemTest {

    @Test
    void gatherersProduceResourcesEachInterval() {
        City city = TestFixtures.newCityWithCitizens("Testburg", 1, 3, 0);
        SimulationConfig config = new SimulationConfig();
        ResourceProductionSystem system = new ResourceProductionSystem();

        system.tick(city, config, 0);

        assertEquals(3 * config.woodPerGatherer, city.getResources().get(ResourceType.WOOD));
        assertEquals(3 * config.stonePerGatherer, city.getResources().get(ResourceType.STONE));
        assertEquals(3 * config.ironPerGatherer, city.getResources().get(ResourceType.IRON));
    }

    @Test
    void citizensConsumeFoodEachInterval() {
        City city = TestFixtures.newCityWithCitizens("Testburg", 1, 0, 0);
        TestFixtures.giveResources(city, 10, 0, 0, 0);
        SimulationConfig config = new SimulationConfig();
        ResourceProductionSystem system = new ResourceProductionSystem();

        system.tick(city, config, 0);

        // 1 citizen consumes foodConsumedPerCitizen, no gatherers produce food.
        assertEquals(10 - config.foodConsumedPerCitizen, city.getResources().get(ResourceType.FOOD));
    }

    @Test
    void starvationClampsFoodAtZeroAndReducesStability() {
        City city = TestFixtures.newCityWithCitizens("Testburg", 1, 0, 0);
        TestFixtures.giveResources(city, 0, 0, 0, 0);
        int startingStability = city.getStability();
        ResourceProductionSystem system = new ResourceProductionSystem();

        system.tick(city, new SimulationConfig(), 0);

        assertEquals(0, city.getResources().get(ResourceType.FOOD));
        assertTrue(city.getStability() < startingStability);
    }

    @Test
    void productionPenaltyHalvesOutput() {
        City city = TestFixtures.newCityWithCitizens("Testburg", 1, 4, 0);
        city.setProductionPenaltyEndTick(1000);
        SimulationConfig config = new SimulationConfig();
        ResourceProductionSystem system = new ResourceProductionSystem();

        system.tick(city, config, 500);

        assertEquals((long) (4 * config.woodPerGatherer * 0.5), city.getResources().get(ResourceType.WOOD));
    }
}
