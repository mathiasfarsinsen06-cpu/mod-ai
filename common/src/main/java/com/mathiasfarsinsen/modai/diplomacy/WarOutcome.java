package com.mathiasfarsinsen.modai.diplomacy;

import com.mathiasfarsinsen.modai.city.City;

/** The result of a single {@link WarResolver} skirmish resolution. */
public final class WarOutcome {

    /** Whether either side actually fought this cycle. */
    public enum Type { VICTORY, STALEMATE }

    private final Type type;
    private final City winner;
    private final City loser;

    private WarOutcome(Type type, City winner, City loser) {
        this.type = type;
        this.winner = winner;
        this.loser = loser;
    }

    static WarOutcome victory(City winner, City loser) {
        return new WarOutcome(Type.VICTORY, winner, loser);
    }

    static WarOutcome stalemate(City a, City b) {
        return new WarOutcome(Type.STALEMATE, null, null);
    }

    public Type getType() {
        return type;
    }

    public City getWinner() {
        return winner;
    }

    public City getLoser() {
        return loser;
    }

    /** A human-readable summary suitable for the server log or a command response. */
    public String describe() {
        if (type == Type.STALEMATE) {
            return "The war rages on with no decisive skirmish this cycle.";
        }
        return winner.getName() + " won a skirmish against " + loser.getName()
                + " (loser stability now " + loser.getStability() + ", population " + loser.getPopulation() + ")";
    }
}
