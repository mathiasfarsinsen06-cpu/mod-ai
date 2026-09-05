package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.Citizen;
import com.mathiasfarsinsen.modai.city.Position;
import com.mathiasfarsinsen.modai.city.ResourceType;

import java.util.UUID;

/** Shared helpers for building test cities. */
final class TestFixtures {
    private TestFixtures() {
    }

    static City newCity(String name) {
        return new City(UUID.randomUUID(), name, new Position(0, 64, 0));
    }

    static City newCityWithCitizens(String name, int leaders, int gatherers, int guards) {
        City city = newCity(name);
        for (int i = 0; i < leaders; i++) {
            city.addCitizen(new Citizen(UUID.randomUUID(), CityRole.LEADER));
        }
        for (int i = 0; i < gatherers; i++) {
            city.addCitizen(new Citizen(UUID.randomUUID(), CityRole.GATHERER));
        }
        for (int i = 0; i < guards; i++) {
            city.addCitizen(new Citizen(UUID.randomUUID(), CityRole.GUARD));
        }
        return city;
    }

    static void giveResources(City city, long food, long wood, long stone, long iron) {
        city.getResources().set(ResourceType.FOOD, food);
        city.getResources().set(ResourceType.WOOD, wood);
        city.getResources().set(ResourceType.STONE, stone);
        city.getResources().set(ResourceType.IRON, iron);
    }
}
