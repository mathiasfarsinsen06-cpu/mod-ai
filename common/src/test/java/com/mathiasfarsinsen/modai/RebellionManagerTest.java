package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.naming.CityNameGenerator;
import com.mathiasfarsinsen.modai.rebellion.RebellionConfig;
import com.mathiasfarsinsen.modai.rebellion.RebellionManager;
import com.mathiasfarsinsen.modai.rebellion.RebellionOutcome;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RebellionManagerTest {

    @Test
    void noRebellionWhenStabilityIsHigh() {
        City city = TestFixtures.newCityWithCitizens("Stableburg", 1, 5, 2);
        city.setStability(90);
        RebellionManager manager = new RebellionManager();
        RebellionConfig config = new RebellionConfig();

        RebellionOutcome outcome = manager.check(city, config, new Random(0), new CityNameGenerator(0), 0);

        assertEquals(RebellionOutcome.Type.NONE, outcome.getType());
    }

    @Test
    void lowStabilityCanTriggerRebellion() {
        City city = TestFixtures.newCityWithCitizens("Unrestville", 1, 10, 2);
        city.setStability(5);
        RebellionManager manager = new RebellionManager();
        RebellionConfig config = new RebellionConfig();
        config.triggerChance = 1.0; // deterministic: always eligible rebellions trigger

        RebellionOutcome outcome = manager.check(city, config, new Random(123), new CityNameGenerator(123), 100);

        assertNotEquals(RebellionOutcome.Type.NONE, outcome.getType());
    }

    @Test
    void citySplitCreatesSplinterWithSharedCitizens() {
        City city = TestFixtures.newCityWithCitizens("Bigburg", 1, 10, 2);
        city.setStability(1);
        RebellionManager manager = new RebellionManager();
        RebellionConfig config = new RebellionConfig();
        config.triggerChance = 1.0;
        config.splitChance = 1.0;
        config.minPopulationToSplit = 2;
        int startingPopulation = city.getPopulation();

        RebellionOutcome outcome = manager.check(city, config, new Random(7), new CityNameGenerator(7), 0);

        assertEquals(RebellionOutcome.Type.CITY_SPLIT, outcome.getType());
        City splinter = outcome.getSplinterCity().orElseThrow();
        assertTrue(splinter.getPopulation() > 0);
        assertEquals(startingPopulation, city.getPopulation() + splinter.getPopulation());
        assertTrue(splinter.getCitizensByRole(CityRole.LEADER).size() >= 1);
    }

    @Test
    void productionPenaltyOutcomeSetsExpiryTick() {
        City city = TestFixtures.newCityWithCitizens("Smallburg", 1, 0, 0);
        city.getCitizens().clear(); // no citizens left to promote -> falls back to production penalty
        city.setStability(1);
        RebellionManager manager = new RebellionManager();
        RebellionConfig config = new RebellionConfig();
        config.triggerChance = 1.0;
        config.splitChance = 0.0;

        RebellionOutcome outcome = manager.check(city, config, new Random(5), new CityNameGenerator(5), 1000);

        assertEquals(RebellionOutcome.Type.PRODUCTION_PENALTY, outcome.getType());
        assertTrue(city.isProductionPenalized(1000));
        assertTrue(city.getProductionPenaltyEndTick() > 1000);
    }
}
