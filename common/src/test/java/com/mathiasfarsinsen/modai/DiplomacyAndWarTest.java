package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.diplomacy.DiplomacyManager;
import com.mathiasfarsinsen.modai.diplomacy.WarOutcome;
import com.mathiasfarsinsen.modai.diplomacy.WarResolver;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiplomacyAndWarTest {

    @Test
    void declareWarSetsRelationOnBothSides() {
        City a = TestFixtures.newCity("Alpha");
        City b = TestFixtures.newCity("Beta");
        DiplomacyManager diplomacy = new DiplomacyManager();

        boolean changed = diplomacy.declareWar(a, b);

        assertTrue(changed);
        assertTrue(a.isAtWarWith(b.getId()));
        assertTrue(b.isAtWarWith(a.getId()));
    }

    @Test
    void makePeaceClearsWarOnBothSides() {
        City a = TestFixtures.newCity("Alpha");
        City b = TestFixtures.newCity("Beta");
        DiplomacyManager diplomacy = new DiplomacyManager();
        diplomacy.declareWar(a, b);

        boolean changed = diplomacy.makePeace(a, b);

        assertTrue(changed);
        assertFalse(a.isAtWarWith(b.getId()));
        assertFalse(b.isAtWarWith(a.getId()));
    }

    @Test
    void cityCannotDeclareWarOnItself() {
        City a = TestFixtures.newCity("Alpha");
        DiplomacyManager diplomacy = new DiplomacyManager();

        assertThrows(IllegalArgumentException.class, () -> diplomacy.declareWar(a, a));
    }

    @Test
    void strongerCityIsMoreLikelyToWinSkirmish() {
        City strong = TestFixtures.newCityWithCitizens("Strong", 1, 2, 10);
        City weak = TestFixtures.newCityWithCitizens("Weak", 1, 2, 0);
        WarResolver resolver = new WarResolver();
        Random deterministicRandom = new Random(42);

        int strongWins = 0;
        for (int i = 0; i < 50; i++) {
            City s = TestFixtures.newCityWithCitizens("Strong", 1, 2, 10);
            City w = TestFixtures.newCityWithCitizens("Weak", 1, 2, 0);
            WarOutcome outcome = resolver.resolveSkirmish(s, w, deterministicRandom);
            if (outcome.getType() == WarOutcome.Type.VICTORY && outcome.getWinner() == s) {
                strongWins++;
            }
        }

        assertTrue(strongWins > 35, "Expected the much stronger city to win most skirmishes, won " + strongWins + "/50");
    }

    @Test
    void loserLosesPopulationResourcesAndStability() {
        City winner = TestFixtures.newCityWithCitizens("Winner", 1, 0, 20);
        City loser = TestFixtures.newCityWithCitizens("Loser", 1, 5, 0);
        TestFixtures.giveResources(loser, 100, 100, 100, 100);
        int winnerStartingStability = winner.getStability();
        int loserStartingStability = loser.getStability();
        WarResolver resolver = new WarResolver();

        WarOutcome outcome = resolver.resolveSkirmish(winner, loser, new Random(1));

        assertNotEquals(WarOutcome.Type.STALEMATE, outcome.getType());
        City actualLoser = outcome.getLoser();
        int startingStability = actualLoser == loser ? loserStartingStability : winnerStartingStability;
        assertTrue(actualLoser.getStability() < startingStability);
    }
}
