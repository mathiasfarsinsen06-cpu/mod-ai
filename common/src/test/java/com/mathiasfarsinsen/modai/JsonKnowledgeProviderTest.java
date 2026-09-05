package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.knowledge.Blueprint;
import com.mathiasfarsinsen.modai.knowledge.Guide;
import com.mathiasfarsinsen.modai.knowledge.JsonKnowledgeProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonKnowledgeProviderTest {

    @Test
    void recommendsHighestTierAffordableBlueprint() {
        Blueprint cheap = new Blueprint("basic_farm", "Basic Farm", "farm", "desc", 1,
                Map.of("WOOD", 5L));
        Blueprint expensive = new Blueprint("iron_farm", "Iron Farm", "farm", "desc", 3,
                Map.of("IRON", 50L));
        JsonKnowledgeProvider provider = new JsonKnowledgeProvider(List.of(cheap, expensive), List.of());

        City city = TestFixtures.newCity("Testburg");
        TestFixtures.giveResources(city, 0, 10, 0, 0); // enough wood, no iron

        Optional<Blueprint> recommendation = provider.recommendBlueprint(city);

        assertTrue(recommendation.isPresent());
        assertEquals("basic_farm", recommendation.get().getId());
    }

    @Test
    void recommendsHigherTierWhenAffordable() {
        Blueprint cheap = new Blueprint("basic_farm", "Basic Farm", "farm", "desc", 1,
                Map.of("WOOD", 5L));
        Blueprint expensive = new Blueprint("iron_farm", "Iron Farm", "farm", "desc", 3,
                Map.of("IRON", 50L));
        JsonKnowledgeProvider provider = new JsonKnowledgeProvider(List.of(cheap, expensive), List.of());

        City city = TestFixtures.newCity("Testburg");
        TestFixtures.giveResources(city, 0, 10, 0, 100);

        Optional<Blueprint> recommendation = provider.recommendBlueprint(city);

        assertTrue(recommendation.isPresent());
        assertEquals("iron_farm", recommendation.get().getId());
    }

    @Test
    void unknownResourceKeysAreIgnoredNotFatal() {
        Blueprint weird = new Blueprint("mystery", "Mystery", "other", "desc", 1,
                Map.of("MITHRIL", 999L));
        JsonKnowledgeProvider provider = new JsonKnowledgeProvider(List.of(weird), List.of());

        City city = TestFixtures.newCity("Testburg");

        Optional<Blueprint> recommendation = provider.recommendBlueprint(city);

        assertTrue(recommendation.isPresent());
        assertEquals("mystery", recommendation.get().getId());
    }

    @Test
    void guidesAreExposedAsProvided() {
        Guide guide = new Guide("housing_101", "Better Housing", List.of("step1", "step2"));
        JsonKnowledgeProvider provider = new JsonKnowledgeProvider(List.of(), List.of(guide));

        assertEquals(1, provider.getGuides().size());
        assertEquals("Better Housing", provider.getGuides().get(0).getTopic());
    }
}
