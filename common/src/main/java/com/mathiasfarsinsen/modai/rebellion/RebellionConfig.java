package com.mathiasfarsinsen.modai.rebellion;

/**
 * Configurable thresholds and effects for the rebellion system, as required
 * by the spec ("Skal være konfigurerbart i config-fil"). The Forge module
 * binds these fields to a {@code ForgeConfigSpec}; sensible defaults are
 * provided here so the logic works even if configuration fails to load.
 */
public final class RebellionConfig {

    /** Stability at or below this value makes a rebellion roll possible each cycle. */
    public int stabilityThreshold = 25;

    /** Probability (0.0-1.0) that a rebellion actually triggers once eligible, per cycle. */
    public double triggerChance = 0.15;

    /** Probability that an eligible rebellion splits the city instead of just replacing the leader. */
    public double splitChance = 0.25;

    /** How many ticks a production penalty lasts after a rebellion. */
    public int productionPenaltyDurationTicks = 6000; // ~5 minutes at 20 tps

    /** Fraction (0.0-1.0) by which production is reduced during the penalty period. */
    public double productionPenaltyFraction = 0.5;

    /** Minimum population required for a city to be eligible to split. */
    public int minPopulationToSplit = 6;

    public RebellionConfig() {
    }

    public RebellionConfig(int stabilityThreshold, double triggerChance, double splitChance,
                            int productionPenaltyDurationTicks, double productionPenaltyFraction,
                            int minPopulationToSplit) {
        this.stabilityThreshold = stabilityThreshold;
        this.triggerChance = triggerChance;
        this.splitChance = splitChance;
        this.productionPenaltyDurationTicks = productionPenaltyDurationTicks;
        this.productionPenaltyFraction = productionPenaltyFraction;
        this.minPopulationToSplit = minPopulationToSplit;
    }
}
