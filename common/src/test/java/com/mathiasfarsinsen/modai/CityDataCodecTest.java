package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.RelationType;
import com.mathiasfarsinsen.modai.city.ResourceType;
import com.mathiasfarsinsen.modai.persistence.CityDataCodec;
import com.mathiasfarsinsen.modai.persistence.CityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CityDataCodecTest {

    @Test
    void cityDataSurvivesJsonRoundTrip() {
        CityManager manager = new CityManager();
        City alpha = TestFixtures.newCityWithCitizens("Alpha", 1, 3, 2);
        TestFixtures.giveResources(alpha, 10, 20, 30, 40);
        alpha.setStability(42);
        City beta = TestFixtures.newCityWithCitizens("Beta", 1, 1, 1);
        alpha.setRelation(beta.getId(), RelationType.WAR);
        beta.setRelation(alpha.getId(), RelationType.WAR);
        manager.addCity(alpha);
        manager.addCity(beta);

        String json = CityDataCodec.toJson(manager);
        CityManager reloaded = CityDataCodec.fromJson(json);

        assertEquals(2, reloaded.size());
        City reloadedAlpha = reloaded.getCity(alpha.getId()).orElseThrow();
        assertEquals("Alpha", reloadedAlpha.getName());
        assertEquals(6, reloadedAlpha.getPopulation());
        assertEquals(42, reloadedAlpha.getStability());
        assertEquals(10, reloadedAlpha.getResources().get(ResourceType.FOOD));
        assertEquals(40, reloadedAlpha.getResources().get(ResourceType.IRON));
        assertTrue(reloadedAlpha.isAtWarWith(beta.getId()));
        assertEquals(1, reloadedAlpha.getCitizensByRole(CityRole.LEADER).size());
    }

    @Test
    void malformedJsonFallsBackToEmptyManager() {
        CityManager manager = CityDataCodec.fromJson("{ not valid json at all");
        assertEquals(0, manager.size());
    }

    @Test
    void blankOrNullJsonYieldsEmptyManager() {
        assertEquals(0, CityDataCodec.fromJson(null).size());
        assertEquals(0, CityDataCodec.fromJson("").size());
    }

    @Test
    void cityLookupByNameIsCaseInsensitive() {
        CityManager manager = new CityManager();
        City city = TestFixtures.newCity("Ironhold");
        manager.addCity(city);

        assertTrue(manager.getCityByName("ironhold").isPresent());
        assertTrue(manager.getCityByName("IRONHOLD").isPresent());
    }
}
