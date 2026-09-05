package com.mathiasfarsinsen.modai.naming;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Generates thematically fantasy/Minecraft-appropriate city names.
 *
 * <p>This is a local, deterministic (seedable) generator — it never calls
 * out to the internet or any external AI service at runtime, satisfying the
 * requirement that gameplay must not depend on live web access. The
 * "AI-generated" feel comes from combinatorial synthesis of syllables and
 * thematic prefixes/suffixes, similar to classic procedural name generators.</p>
 *
 * <p>Instances track previously issued names so that repeated calls on the
 * same instance (typically one per world) never return a duplicate,
 * satisfying the "unique name" requirement. Callers that reload names from
 * persistence should call {@link #reserve(String)} for each existing name
 * before generating new ones.</p>
 */
public final class CityNameGenerator {

    private static final String[] PREFIXES = {
            "Aer", "Bal", "Cor", "Dun", "El", "Fen", "Grim", "Hal", "Iron", "Jor",
            "Kar", "Lor", "Mor", "Nor", "Oak", "Pel", "Quen", "Rav", "Sil", "Thal",
            "Ul", "Val", "Wynd", "Xar", "Yor", "Zeph"
    };

    private static final String[] SUFFIXES = {
            "burg", "haven", "hold", "ton", "wick", "gard", "fall", "moor", "reach", "spire",
            "vale", "watch", "wood", "shire", "keep", "port", "ridge", "stead", "crest", "mere"
    };

    private final Random random;
    private final Set<String> used = new HashSet<>();

    public CityNameGenerator() {
        this(new Random());
    }

    public CityNameGenerator(long seed) {
        this(new Random(seed));
    }

    public CityNameGenerator(Random random) {
        this.random = random;
    }

    /** Marks {@code name} as already in use, e.g. when loading persisted cities. */
    public void reserve(String name) {
        if (name != null) {
            used.add(name);
        }
    }

    /**
     * Generates a new, unique city name.
     *
     * <p>Falls back to appending a numeric suffix after a bounded number of
     * collisions, guaranteeing termination even if the combinatorial space
     * is exhausted.</p>
     */
    public String generate() {
        String base = null;
        for (int attempt = 0; attempt < 64; attempt++) {
            String candidate = PREFIXES[random.nextInt(PREFIXES.length)]
                    + SUFFIXES[random.nextInt(SUFFIXES.length)];
            if (!used.contains(candidate)) {
                base = candidate;
                break;
            }
        }
        if (base == null) {
            // Extremely unlikely fallback: combine two prefixes.
            base = PREFIXES[random.nextInt(PREFIXES.length)] + PREFIXES[random.nextInt(PREFIXES.length)];
        }
        String result = base;
        int suffixCounter = 2;
        while (used.contains(result)) {
            result = base + " " + suffixCounter;
            suffixCounter++;
        }
        used.add(result);
        return result;
    }
}
