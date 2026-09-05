package com.mathiasfarsinsen.modai.knowledge;

import com.mathiasfarsinsen.modai.city.City;

import java.util.List;
import java.util.Optional;

/**
 * Feeds villager/AI decision-making with local, structured strategy data
 * (blueprints and guides). Implementations must not rely on network access
 * at runtime — knowledge is authored offline (e.g. as datapack JSON) and
 * simply read here.
 */
public interface KnowledgeProvider {

    List<Blueprint> getBlueprints();

    List<Guide> getGuides();

    /**
     * Recommends the best next blueprint for {@code city} to pursue, given
     * its current resource stockpile. Implementations should prefer higher
     * tier blueprints that the city can currently afford.
     */
    Optional<Blueprint> recommendBlueprint(City city);
}
