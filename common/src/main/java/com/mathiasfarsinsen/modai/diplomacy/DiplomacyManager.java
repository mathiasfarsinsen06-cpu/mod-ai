package com.mathiasfarsinsen.modai.diplomacy;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.RelationType;

/**
 * Manages diplomatic relations (peace/war) between cities.
 *
 * <p>Relations are stored symmetrically: declaring war or making peace
 * updates both cities' relation maps so that either side can be queried
 * independently.</p>
 */
public final class DiplomacyManager {

    /** Declares war between the two cities. Returns {@code false} if already at war. */
    public boolean declareWar(City a, City b) {
        if (a.getId().equals(b.getId())) {
            throw new IllegalArgumentException("A city cannot declare war on itself");
        }
        boolean wasAlreadyAtWar = a.isAtWarWith(b.getId());
        a.setRelation(b.getId(), RelationType.WAR);
        b.setRelation(a.getId(), RelationType.WAR);
        return !wasAlreadyAtWar;
    }

    /** Makes peace between the two cities. Returns {@code false} if already at peace. */
    public boolean makePeace(City a, City b) {
        boolean wasAtWar = a.isAtWarWith(b.getId());
        a.setRelation(b.getId(), RelationType.PEACE);
        b.setRelation(a.getId(), RelationType.PEACE);
        return wasAtWar;
    }

    public boolean areAtWar(City a, City b) {
        return a.isAtWarWith(b.getId());
    }
}
